package fr.flafla.generator.access;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

public class Method extends AbstractElement<ExecutableElement> {
	protected final ExecutableType type;
	private List<Parameter> parameters;

	public Method(ExecutableType type, ExecutableElement elt) {
		super(elt);
		this.type = type;
	}

	public Method(Method method) {
		super(method.elt);
		this.type = method.type;
	}

	public String getName() {
		return elt.getSimpleName().toString();
	}

	public String getDocument() {
		return Environment.get().getElementUtils().getDocComment(elt);
	}

	public Iterable<? extends Parameter> getParameters() {
		if (this.parameters == null) {
			final List<? extends TypeMirror> parameters = type.getParameterTypes();
			final List<? extends VariableElement> eltParameters = elt.getParameters();
			this.parameters = new ArrayList<Parameter>(parameters.size());
			for (int i = 0; i < eltParameters.size(); i++) {
				VariableElement elt = eltParameters.get(i);
				final TypeMirror mirror = parameters.get(i);
				this.parameters.add(new Parameter(elt, mirror, mirror.getKind(), elt.getSimpleName().toString()));
			}
		}
		return this.parameters;
	}

	public List<Type> getThrows() {
		final List<? extends TypeMirror> throwsType = type.getThrownTypes();
		final List<Type> result = new ArrayList<Type>(throwsType.size());
		for (int i = 0; i < throwsType.size(); i++) {
			result.add(new Type(throwsType.get(i)));
		}
		return result;
	}

	public List<Type> getThrowsAndRuntime() {
		final TypeMirror runtimeExceptionType = Environment.get().getElementUtils().getTypeElement("java.lang.RuntimeException").asType();

		final List<? extends TypeMirror> throwsType = type.getThrownTypes();
		final List<Type> result = new ArrayList<Type>(throwsType.size());
		boolean withRuntime = true;
		for (int i = 0; i < throwsType.size(); i++) {
			final TypeMirror throwType = throwsType.get(i);
			result.add(new Type(throwType));
			if (withRuntime && Environment.get().getTypeUtils().isAssignable(runtimeExceptionType, throwType))
				withRuntime = false;
		}
		if (withRuntime) {
			result.add(new Type(runtimeExceptionType));
		}
		return result;
	}

	public String getBoxedReturnType() {
		final TypeMirror type = elt.getReturnType();
		if (type.getKind() == TypeKind.VOID)
			return "Void";

		return new Type(type).getCompleteName();
	}

	public boolean getHasResult() {
		return elt.getReturnType().getKind() != TypeKind.VOID;
	}

	public boolean getIsRealMethod() {
		return !getIsConstructor();
	}

	public boolean getIsConstructor() {
		return "<init>".equals(getName());
	}

	public String getReturnType() {
		try {
			final TypeMirror returnType = type.getReturnType();
			return TypeUtil.getTypeName((DeclaredType) elt.getEnclosingElement().asType(), returnType);
		} catch (RuntimeException e) {
			System.err.println("error on method " + getName());
			e.printStackTrace();
			throw e;
		}
	}
	
	public String getReturnTypeRaw() {
		try {
			final TypeMirror returnType = type.getReturnType();
			return TypeUtil.getTypeNameRaw((DeclaredType) elt.getEnclosingElement().asType(), returnType);
		} catch (RuntimeException e) {
			System.err.println("error on method " + getName());
			e.printStackTrace();
			throw e;
		}
	}

	public String getField() {
		final String name = getName();
		if (name.startsWith("get") || name.startsWith("set")) {
			return Character.toLowerCase(name.charAt(3)) + name.substring(4);
		}
		return name;
	}

	public String getConst() {
		try {
			final String field = getField();
			return field.replaceAll("([A-Z])", "_$1").toUpperCase();
		} catch (RuntimeException e) {
			e.printStackTrace();
			throw e;
		}
	}

	public boolean isSubsignature(ExecutableType method) {
		final ExecutableElement methodElement = (ExecutableElement) Environment.get().getTypeUtils().asElement(method);
		return Environment.get().getElementUtils().overrides(elt, methodElement, (TypeElement) elt.getEnclosingElement());
		//				return Environment.get().getTypeUtils().isSubsignature(type, method);
	}

	public boolean getIsOverridable() {
		return getIsRealMethod() && getIsPublic() && !getIsStatic() && !getIsFinal();
	}

	public boolean getIsPublic() {
		return elt.getModifiers().contains(Modifier.PUBLIC);
	}

	public boolean getIsStatic() {
		return elt.getModifiers().contains(Modifier.STATIC);
	}

	public boolean getIsFinal() {
		return elt.getModifiers().contains(Modifier.FINAL);
	}

	public String getSignature() {
		final StringBuilder sb = new StringBuilder(getName());
		sb.append('(');
		boolean notFirst = false;
		for (Parameter parameter : getParameters()) {
			if (notFirst) {
				sb.append(',');
			} else {
				notFirst = true;
			}
			sb.append(parameter.getTypeName());
		}
		sb.append(')');
		return sb.toString();
	}
	
	public Type getEnclosingType() {
		return new Type(elt.getEnclosingElement().asType());
	}
}
