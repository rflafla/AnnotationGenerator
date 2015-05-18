package fr.flafla.generator.access;

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

public class Parameter extends AbstractElement<VariableElement> {
	protected String arg;
	protected TypeMirror type;
	private final TypeKind kind;

	public Parameter(VariableElement elt, TypeMirror type, TypeKind kind, String arg) {
		super(elt);
		this.type = type;
		this.arg = arg;
		this.kind = kind;
	}
	
	public Parameter(Parameter parameter) {
		super(parameter.elt);
		this.type = parameter.type;
		this.arg = parameter.arg;
		this.kind = parameter.kind;
	}

	public Type getType() {
		return new Type(type);
	}

	public String getTypeName() {
		try {
			return TypeUtil.getTypeName((DeclaredType) elt.getEnclosingElement().getEnclosingElement().asType(), type);
		} catch (final RuntimeException e) {
			System.err.println(kind);
			e.printStackTrace();
			throw e;
		}
	}

	public String getName() {
		return arg;
	}

	public boolean isEquivalent(Parameter parameter) {
		return parameter != null && parameter.getName().equals(getName()) && parameter.getType().equals(getType());
	}

}
