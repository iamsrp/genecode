# genecode

Genetic algorithms are not dead, they're just resting...

Overview
--------
The genecode package is a genetic algorithms implementation which aims to mimic the way biological viruses behave in the real world. It uses a different set of underlying principles from "classic" genetic algorithms but the overall idea is roughly the same.

It requires Java8 and `ant` to build but, aside from that, should not need anything special. See the JavaDoc, and the `example` package, for more information.

It currently works with a cut-down type system which supports various primitive Java types within a basic functional programming language.

Examples
--------

The package supports a small number of primitive types (boolean, number, strings and arrays). On the numerical front, it can solve quadractics and quartics reasonably well. As an example, though, here's something which solves a string-to-string mapping.

An example of a function written by the package is one which attempts to create the mapping from "Firstname Lastname" to "Lastname, F."; this comes from the `NamingSolver` class in the `example` package and happens to be pretty optimal. The resultant code has been formatted and annotated for clarity.

```
# Join together the last pair of strings, "Lastname, " and "F.",
# to produce the final value
StringConcat(
    # Join together "Lastname," with " "
    StringConcat(
        # Join together the "Lastname" with ","
        StringConcat(
            GetAt(
                StringSplit(
                    Accessor[String:"name"],
                    " "
                ),
                -1
            ),
            ","
        ),
        " "
    ),
    # Join together the initial with "."
    StringConcat(
        # Pick the first character from "Firstname"
        Substring(
            Accessor[String:"name"],
            0,
            1
        ),
        "."
    )
)
```

The above solution took about 20hrs and 474,300 iterations (producing nearly 6 billion genomes) on desktop computer.

Here's an earlier attempt. This one happened to find, and exploit, a bug in an early implementation of StringSplit(). It took about 7hrs and 162,311 iterations (producing around 2 billion genomes):

```
# Concatenate all the string array elements into a string
Reduce[StringConcat](
    # Put a '.' at the end of the (singleton) array
    InsertAt(
        # These map operations eventually yield a singleton
        # array with the contents ["Surname, N"]
        Map[StringConcat](
            Map[StringConcat](
                # Take the "Name Surname" string and
                # turn it into a ["Surname", "Name"] array
                Reverse(
                    StringSplit(Accessor["name"]," ")
                ),
                # Create the singleton array [", "]
                Map[StringConcat](
                    # A contrived way to turn a string
                    # into a singleton array of that string
                    StringSplit(",","."),
                    StringSplit(" ","u")
                )
            ),
            # Exploit a bug(!) in StringSplit whereby splitting a
            # string by itself yields an array with the first
            # character in it: "Name Surname" -> ["N"]
            StringSplit(
                Accessor["name" <String>],
                Accessor["name" <String>]
            )
        ),
        1,
        "."
    )
)
```
