package org.bytedeco.javacpp.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.tools.Generator;

/**
 * Specifies a C++ class to act as an adapter to convert the types of arguments.
 * Two such C++ classes made available by {@link Generator} are {@code StringAdapter}
 * and {@code VectorAdapter}, to bridge a few differences between {@code std::string}
 * and {@link String}; and between {@code std::vector}, Java arrays of primitive types,
 * and {@link Pointer}. Adapter classes must define the following public members:
 * <ul>
 * <li> A constructor accepting 2 arguments (or more if {@link #argc()} &gt; 1): a pointer and a size</li>
 * <li> Another constructor that accepts a reference to the object of the other class</li>
 * <li> A {@code static void deallocate(pointer)} function</li>
 * <li> Overloaded cast operators to both types, for references and pointers</li>
 * <li> A {@code void assign(pointer, size)} function</li>
 * <li> A {@code size} member variable for arrays accessed via pointer</li>
 * </ul>
 * To reduce further the amount of coding, this annotation can also be used on
 * other annotations, such as with {@link StdString} and {@link StdVector}.
 *
 * @see Generator
 *
 * @author Samuel Audet
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
public @interface Adapter {
    /** 
     * @return the name of the C++ adapter class.
     */
    String value();
    
    /** 
     * The number of arguments that {@link Generator} takes from the method as
     * arguments to the adapter constructor. 
     * 
     * @return the number of arguments.
     */
    int argc() default 1;
}