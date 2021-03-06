package com.mantledillusion.vaadin.cotton.model;

import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import com.mantledillusion.data.epiphy.interfaces.ListedProperty;
import com.mantledillusion.data.epiphy.interfaces.ReadableProperty;
import com.mantledillusion.data.epiphy.interfaces.WriteableProperty;
import com.mantledillusion.injection.hura.Injector;
import com.mantledillusion.injection.hura.Predefinable.Singleton;
import com.mantledillusion.injection.hura.Processor.Phase;
import com.mantledillusion.injection.hura.annotation.Adjust;
import com.mantledillusion.injection.hura.annotation.Adjust.MappingDef;
import com.mantledillusion.injection.hura.annotation.Construct;
import com.mantledillusion.injection.hura.annotation.Inject;
import com.mantledillusion.injection.hura.annotation.Optional;
import com.mantledillusion.injection.hura.annotation.Process;
import com.mantledillusion.vaadin.cotton.exception.WebException;
import com.mantledillusion.vaadin.cotton.exception.WebException.HttpErrorCodes;
import com.vaadin.data.HasValue;

/**
 * Model accessor that allows indexable access on a parent {@link ModelProxy}
 * which can either be a {@link ModelContainer} or another
 * {@link ModelAccessor}.
 * <p>
 * Model accessing via this {@link ModelAccessor} will then be forwarded to that
 * parent {@link ModelProxy}.
 * <p>
 * When a {@link ModelContainer} is required as the parent, the
 * {@link ModelAccessor} does not need to be extended; @{@link Inject} a
 * {@link Singleton} {@link ModelContainer} somewhere else using the singletonId
 * {@link ModelContainer#DEFAULT_SINGLETON_ID}. When handling multiple
 * {@link ModelContainer}s that could be used,
 * an @{@link Adjust} @{@link MappingDef} can re-map that singletonId to a
 * specific value.
 * <p>
 * When a {@link ModelAccessor} is required as the parent, the
 * {@link ModelAccessor} has to be extended, so the correct parent can be given
 * to {@link #ModelAccessor(ModelProxy)}.
 * <p>
 * NOTE: Should be injected, since the {@link Injector} handles the instance's
 * life cycles.
 *
 * @param <ModelType>
 *            The root type of the data model the {@link ModelAccessor}
 *            accesses.
 */
public class ModelAccessor<ModelType> extends ModelBinder<ModelType> {

	@Inject(IndexContext.SINGLETON_ID)
	@Optional
	private IndexContext indexContext = IndexContext.EMPTY;

	private final ModelProxy<ModelType> parent;
	private final Map<ReadableProperty<ModelType, ?>, Set<PropertyBinding<?>>> boundFields = new IdentityHashMap<>();

	@Construct
	private ModelAccessor(@Inject(ModelContainer.DEFAULT_SINGLETON_ID) ModelContainer<ModelType> parentContainer) {
		this((ModelProxy<ModelType>) parentContainer);
	}

	/**
	 * {@link Constructor}.
	 * <p>
	 * Note that even when using this {@link Constructor}, the {@link ModelAccessor}
	 * still has to be injected to make sure its life cycle is handled.
	 * 
	 * @param parentProxy
	 *            The parent {@link ModelProxy} to use; might <b>not</b> be null.
	 */
	protected ModelAccessor(ModelProxy<ModelType> parentProxy) {
		if (parentProxy == null) {
			throw new WebException(HttpErrorCodes.HTTP901_ILLEGAL_ARGUMENT_ERROR,
					"Cannot create an accessor for a null parent container.");
		}
		this.parent = parentProxy;
		this.parent.register(this);
	}

	// ######################################################################################################################################
	// ############################################################ INTERNAL ################################################################
	// ######################################################################################################################################

	@Process(Phase.DESTROY)
	private void releaseReferences() {
		for (Set<PropertyBinding<?>> bindings : this.boundFields.values()) {
			for (PropertyBinding<?> binding : bindings) {
				binding.destroy();
			}
		}
		this.boundFields.clear();

		this.parent.unregister(this);
	}

	// ######################################################################################################################################
	// ############################################################## INDEX #################################################################
	// ######################################################################################################################################

	@Override
	protected final IndexContext getIndexContext() {
		return this.indexContext;
	}

	// ######################################################################################################################################
	// ########################################################### MODEL CONTROL ############################################################
	// ######################################################################################################################################

	@Override
	public final boolean hasModel() {
		return this.parent.hasModel();
	}

	@Override
	public final ModelType getModel() {
		return this.parent.getModel();
	}

	@Override
	final void setModel(ModelType model) {
		for (ReadableProperty<ModelType, ?> property : this.boundFields.keySet()) {
			for (PropertyBinding<?> binding : this.boundFields.get(property)) {
				binding.update();
			}
		}
		super.setModel(model);
	}

	@Override
	public final boolean isModelChanged() {
		return this.parent.isModelChanged();
	}

	@Override
	public final <PropertyType> boolean isPropertyChanged(ReadableProperty<ModelType, PropertyType> property) {
		return this.parent.isPropertyChanged(property, this.indexContext);
	}

	@Override
	public final <PropertyType> boolean isPropertyChanged(ReadableProperty<ModelType, PropertyType> property,
			IndexContext context) {
		return this.parent.isPropertyChanged(property, this.indexContext.union(context));
	}

	@Override
	public <PropertyType> boolean exists(ReadableProperty<ModelType, PropertyType> property) {
		return this.parent.exists(property, this.indexContext);
	}

	@Override
	public <PropertyType> boolean exists(ReadableProperty<ModelType, PropertyType> property, IndexContext context) {
		return this.parent.exists(property, this.indexContext.union(context));
	}

	// ######################################################################################################################################
	// ###################################################### PROPERTIED MODEL ACCESS #######################################################
	// ######################################################################################################################################

	@Override
	public final <PropertyType> PropertyType getProperty(ReadableProperty<ModelType, PropertyType> property) {
		return this.parent.getProperty(property, this.indexContext);
	}

	@Override
	public final <PropertyType> PropertyType getProperty(ReadableProperty<ModelType, PropertyType> property,
			IndexContext indexContext) {
		return this.parent.getProperty(property, this.indexContext.union(indexContext));
	}

	@Override
	public final <PropertyType> void setProperty(WriteableProperty<ModelType, PropertyType> property,
			PropertyType value) {
		this.parent.setProperty(property, value, this.indexContext);
	}

	@Override
	public final <PropertyType> void setProperty(WriteableProperty<ModelType, PropertyType> property,
			PropertyType value, IndexContext indexContext) {
		this.parent.setProperty(property, value, this.indexContext.union(indexContext));
	}

	@Override
	public final <PropertyType> void addProperty(ListedProperty<ModelType, PropertyType> property, PropertyType value) {
		this.parent.addProperty(property, value, this.indexContext);
	}

	@Override
	public final <PropertyType> void addProperty(ListedProperty<ModelType, PropertyType> property, PropertyType value,
			IndexContext indexContext) {
		this.parent.addProperty(property, value, this.indexContext.union(indexContext));
	}

	@Override
	public final <PropertyType> PropertyType removeProperty(ListedProperty<ModelType, PropertyType> property) {
		return this.parent.removeProperty(property, this.indexContext);
	}

	@Override
	public <PropertyType> Integer removeProperty(ListedProperty<ModelType, PropertyType> property,
			PropertyType element) {
		return this.parent.removeProperty(property, element, this.indexContext);
	}
	
	@Override
	public final <PropertyType> PropertyType removeProperty(ListedProperty<ModelType, PropertyType> property,
			IndexContext indexContext) {
		return this.parent.removeProperty(property, this.indexContext.union(indexContext));
	}

	@Override
	public <PropertyType> Integer removeProperty(ListedProperty<ModelType, PropertyType> property, PropertyType element,
			IndexContext indexContext) {
		return this.parent.removeProperty(property, element, this.indexContext.union(indexContext));
	}

	// ######################################################################################################################################
	// ############################################################## BINDING ###############################################################
	// ######################################################################################################################################

	@Override
	public final <FieldType extends HasValue<PropertyType>, PropertyType> FieldType bindToProperty(FieldType field,
			ReadableProperty<ModelType, PropertyType> property) {
		if (field == null) {
			throw new WebException(HttpErrorCodes.HTTP901_ILLEGAL_ARGUMENT_ERROR, "Cannot bind a null HasValue.");
		} else if (property == null) {
			throw new WebException(HttpErrorCodes.HTTP901_ILLEGAL_ARGUMENT_ERROR,
					"Cannot bind a HasValue for a null property.");
		}

		bind(property, bindingOf(property, field));

		return field;
	}

	@Override
	public <FieldType extends HasValue<PropertyType>, PropertyType> FieldType bindToProperty(FieldType field,
			ReadableProperty<ModelType, PropertyType> property, InputValidator<PropertyType> inputValidator) {
		if (field == null) {
			throw new WebException(HttpErrorCodes.HTTP901_ILLEGAL_ARGUMENT_ERROR, "Cannot bind a null HasValue.");
		} else if (property == null) {
			throw new WebException(HttpErrorCodes.HTTP901_ILLEGAL_ARGUMENT_ERROR,
					"Cannot bind a HasValue for a null property.");
		} else if (inputValidator == null) {
			throw new WebException(HttpErrorCodes.HTTP901_ILLEGAL_ARGUMENT_ERROR,
					"Cannot bind a HasValue using a null input validator.");
		}

		bind(property, bindingOf(property, field, inputValidator));

		return field;
	}

	@Override
	public final <FieldType extends HasValue<FieldValueType>, FieldValueType, PropertyType> FieldType bindToProperty(
			FieldType field, ReadableProperty<ModelType, PropertyType> property,
			Converter<FieldValueType, PropertyType> converter) {
		if (field == null) {
			throw new WebException(HttpErrorCodes.HTTP901_ILLEGAL_ARGUMENT_ERROR, "Cannot bind a null HasValue.");
		} else if (property == null) {
			throw new WebException(HttpErrorCodes.HTTP901_ILLEGAL_ARGUMENT_ERROR,
					"Cannot bind a HasValue for a null property.");
		} else if (converter == null) {
			throw new WebException(HttpErrorCodes.HTTP901_ILLEGAL_ARGUMENT_ERROR,
					"Cannot bind a HasValue to a differently typed property using a null converter.");
		}

		bind(property, bindingOf(property, field, converter));

		return field;
	}

	private final <PropertyType> void bind(ReadableProperty<ModelType, PropertyType> property,
			PropertyBinding<PropertyType> binding) {
		if (!this.boundFields.containsKey(property)) {
			this.boundFields.put(property, new HashSet<>());
		}
		this.boundFields.get(property).add(binding);
		binding.update();
	}

	// ######################################################################################################################################
	// ############################################################## UPDATE ################################################################
	// ######################################################################################################################################

	final void updatePropertyBoundFields(IndexContext context, Set<ReadableProperty<ModelType, ?>> properties) {
		if (context.contains(this.indexContext)) {
			for (ReadableProperty<ModelType, ?> property : properties) {
				if (this.boundFields.containsKey(property)) {
					for (PropertyBinding<?> binding : this.boundFields.get(property)) {
						binding.update();
					}
				}
			}
			for (ModelAccessor<ModelType> child : getChildren()) {
				child.updatePropertyBoundFields(context, properties);
			}
		}
	}

	final void updatePropertyIndex(ReadableProperty<ModelType, ?> property, int baseIndex, int modification) {
		this.indexContext = this.indexContext.update(property, baseIndex, modification);
		for (ModelAccessor<ModelType> child : getChildren()) {
			child.updatePropertyIndex(property, baseIndex, modification);
		}
	}

	// ######################################################################################################################################
	// ############################################################ VALIDATION ##############################################################
	// ######################################################################################################################################

	@Override
	final void gatherPreevalutationErrors(ValidationErrorRegistry<ModelType> errorRegistry) {
		Set<ValidationError> errors;
		for (ReadableProperty<ModelType, ?> property : this.boundFields.keySet()) {
			for (PropertyBinding<?> binding : this.boundFields.get(property)) {
				errors = binding.getError();
				if (errors != null) {
					for (ValidationError error : errors) {
						errorRegistry.addError(error, property);
					}
				}
			}
		}
		for (ModelAccessor<ModelType> child : getChildren()) {
			child.gatherPreevalutationErrors(errorRegistry);
		}
	}

	@Override
	final void applyErrors(ValidationErrorRegistry<ModelType> errorRegistry) {
		for (ReadableProperty<ModelType, ?> property : this.boundFields.keySet()) {
			for (PropertyBinding<?> binding : this.boundFields.get(property)) {
				binding.setError(errorRegistry);
			}
		}
		for (ModelAccessor<ModelType> child : getChildren()) {
			child.applyErrors(errorRegistry);
		}
	}

	@Override
	final void clearErrors() {
		for (ReadableProperty<ModelType, ?> property : this.boundFields.keySet()) {
			for (PropertyBinding<?> binding : this.boundFields.get(property)) {
				binding.clearError();
			}
		}
		for (ModelAccessor<ModelType> child : getChildren()) {
			child.clearErrors();
		}
	}

	// ######################################################################################################################################
	// ############################################################ PERSISTING ##############################################################
	// ######################################################################################################################################

	@Override
	public final ModelType persist() {
		return this.parent.persist(this.indexContext);
	}

	@Override
	public final ModelType persist(IndexContext context) {
		return this.parent.persist(context);
	}
}
