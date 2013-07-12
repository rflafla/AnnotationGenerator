package fr.flafla.generator.access;

import javax.lang.model.type.DeclaredType;

public class Parameter {
	protected String arg;
	protected DeclaredType type;
	
	public Parameter(DeclaredType type, String arg) {
		this.type = type;
		this.arg = arg;
	}
	
	public Parameter(Parameter parameter) {
		this.type = parameter.type;
		this.arg = parameter.arg;
	}
	
	public Type getType() {
		return new Type(type);
	}
	
	public String getName() {
		return arg;
	}
}