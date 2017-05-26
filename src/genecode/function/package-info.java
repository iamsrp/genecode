/**
 * <p>The functions available to the genecode system.</p>
 *
 * <p>The genecode system can access functions in order to convert
 * input values to output ones. By putting these together (via the
 * genes) more complex programs are generated.</p>
 *
 * <p>The functions provide the basis for a crude functional
 * programming language. That is to say, all the values in the system
 * are immutable and none of the functions have side effects. For
 * exmaple, if you remove a value from an array then what you get back
 * is a new array instance with that value removed; the original array
 * is untouched.</p>
 *
 * <p>Functions are mildy strict about the types which they can
 * accept. By keeping types correct the search-space over which the
 * genome may roam is limited to what is a legal program.</p>
 *
 * <p>Right now only primitive types are available within the
 * system. There are no complex types and no containers, aside from
 * arrays.</p>
 *
 * <p>If a function fails to compute then it will yield {@code
 * null}.</p>
 */
package genecode.function;
