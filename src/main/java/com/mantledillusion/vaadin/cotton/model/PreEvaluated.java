package com.mantledillusion.vaadin.cotton.model;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.mantledillusion.injection.hura.annotation.Validated;
import com.mantledillusion.vaadin.cotton.model.ValidationContext.PreEvaluatedValidator;

/**
 * {@link Annotation} for {@link PropertyValidator}s that points at one or more other
 * {@link PropertyValidator}s whose evaluation results are {@link PreEvaluated.Prerequisite}s for
 * the annotated {@link PropertyValidator} to be evaluated itself.
 */
@Retention(RUNTIME)
@Target(TYPE)
@Validated(PreEvaluatedValidator.class)
public @interface PreEvaluated {
	
	/**
	 * Defines a {@link Prerequisite} of a {@link PropertyValidator}.
	 * <P>
	 * This {@link Prerequisite} will be evaluated before the {@link PropertyValidator}
	 * annotated with @{@link PreEvaluated}; the {@link Prerequisite}'s result will
	 * determine whether the annotated {@link PropertyValidator} is even executed.
	 */
	public @interface Prerequisite {
	
		/**
		 * The {@link Prerequisite}s class type.
		 * 
		 * @return The {@link Class} of the {@link PropertyValidator} implementation to
		 *         instantiate and execute beforehand; never null
		 */
		Class<? extends PropertyValidator<?>> value();
	
		/**
		 * Determines whether this {@link Prerequisite}'s validation result has to be
		 * true (no errors) or false (one or more validation errors) in order for the
		 * {@link PropertyValidator} annotated with @{@link PreEvaluated} to be evaluated.
		 * 
		 * @return True if the {@link Prerequisite} has to evaluate valid, false
		 *         otherwise; true by default
		 */
		boolean requiredResult() default true;
	}

	/**
	 * The {@link PreEvaluated.Prerequisite} {@link PropertyValidator}s the annotated {@link PropertyValidator}
	 * requires to be evaluated beforehand.
	 * 
	 * @return the {@link PreEvaluated.Prerequisite}s to this {@link PropertyValidator}; never null, might be empty
	 */
	Prerequisite[] value();
}