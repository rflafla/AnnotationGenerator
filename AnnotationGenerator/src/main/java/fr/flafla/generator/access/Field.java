package fr.flafla.generator.access;

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

public class Field extends AbstractElement<VariableElement> {

	private final TypeMirror type;

	public Field(VariableElement field, TypeMirror type) {
		super(field);
		this.type = type;
	}
	
	public Field(Field field) {
		super(field.elt);
		this.type = field.type;
	}
	
	public String getName() {
		return elt.getSimpleName().toString();
	}
	
	public Type getType() {
		return new Type(elt.asType());
	}
	
	public String getGetterName() {
		final String name = getName();
		return "get"+Character.toUpperCase(name.charAt(0))+name.substring(1);
	}
	
	public String getTypeName() {
		return TypeUtil.getTypeName((DeclaredType) elt.getEnclosingElement().asType(), type);
	}
	

}
