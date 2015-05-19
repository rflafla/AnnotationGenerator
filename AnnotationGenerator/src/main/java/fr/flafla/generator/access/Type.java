package fr.flafla.generator.access;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class Type extends AbstractElement<TypeElement> {
	private final TypeMirror typeMirror;
	private final String sourcePackage;
	private final String qualifiedName;
	private final String name;
	private boolean array;

	public Type(TypeMirror type) {
		super(getElement(type));
		this.typeMirror = type;

		if (type.getKind().isPrimitive()) {
			qualifiedName = type.getKind().name().toLowerCase();
			name = qualifiedName;
			sourcePackage = null;
			array = false;
		} else {
			array = type.getKind() == TypeKind.ARRAY;
			sourcePackage = getSourcePackage(elt);
			qualifiedName = this.elt.getQualifiedName().toString();
			final int lastPoint = qualifiedName.lastIndexOf('.');
			name = qualifiedName.substring(lastPoint + 1);
		}
	}

	public Type(Class<?> clazz) {
		this(Environment.get().getElementUtils().getTypeElement(clazz.getName()).asType());
	}

	public Type(Type type) {
		this(type.typeMirror);
	}
	
	private static TypeElement getElement(TypeMirror type) {
		if (type.getKind() == TypeKind.ARRAY)
			type = ((ArrayType) type).getComponentType();
		return (TypeElement) Environment.get().getTypeUtils().asElement(type);
	}
	
	protected String getSourcePackage(TypeElement elt) {
		Element packageElement = elt.getEnclosingElement();
		while (!(packageElement instanceof PackageElement)) {
			packageElement = packageElement.getEnclosingElement();
		}
		return ((PackageElement) packageElement).getQualifiedName().toString();
	}

	public boolean getIsArray() {
		return array;
	}
	
	public Type getComponentType() {
		if (array)
			return new Type(((ArrayType) typeMirror).getComponentType());
		return null;
	}
	
	public TypeElement getElement() {
		return elt;
	}

	public TypeMirror getTypeMirror() {
		return typeMirror;
	}

	public String getQualifiedName() {
		return qualifiedName;
	}

	public String getCompleteName() {
		return TypeUtil.getCompleteName(typeMirror);
	}

	public String getName() {
		return name;
	}

	public String getPackage() {
		return sourcePackage;
	}

	public List<Type> getParameters(Class<?> superClass) {
		final List<Type> result = new ArrayList<Type>();
		if (typeMirror instanceof DeclaredType) {
			final List<? extends TypeMirror> args = ((DeclaredType) typeMirror).getTypeArguments();
			for (final TypeMirror arg : args) {
				result.add(new Type(arg));
			}
		}
		return result;
	}

	public Iterable<? extends Type> getInterfaces() {
		final List<Type> result = new ArrayList<Type>();
		if (elt != null) {
			final List<? extends TypeMirror> interfaces = elt.getInterfaces();
			for (final TypeMirror intf : interfaces) {
				result.add(new Type(intf));
			}
		}
		return result;
	}

	public Iterable<? extends Method> getMethods() {
		final List<Method> result = new ArrayList<Method>();
		if (elt != null) {
			final Set<String> names = new HashSet<String>();
			TypeElement superClass = elt;
			// Reverse order
			final Stack<TypeElement> types = new Stack<TypeElement>();
			while (superClass != null && !superClass.getQualifiedName().toString().equals("java.lang.Object")) {
				types.add(superClass);
				superClass = (TypeElement) Environment.get().getTypeUtils().asElement(superClass.getSuperclass());
			}
			while (!types.isEmpty()) {
				final TypeElement typeElement = types.pop();
				extractMethods(names, result, typeElement);
			}
		}

		return result;
	}

	public Iterable<? extends Method> getTypeMethods() {
		final List<Method> result = new ArrayList<Method>();
		if (elt != null) {
			final Set<String> names = new HashSet<String>();
			extractMethods(names, result, elt);
		}
		return result;
	}

	protected void extractMethods(final Set<String> names, final List<Method> result, TypeElement typeElement) {
		final List<? extends Element> typeElements = typeElement.getEnclosedElements();
		for (final Element elt : typeElements) {
			if (elt instanceof ExecutableElement) {
				final ExecutableType executableType = (ExecutableType) Environment.get().getTypeUtils().asMemberOf((DeclaredType) typeMirror, elt);
				final Method method = new Method(executableType, (ExecutableElement) elt);
				if (names.add(method.getSignature())) {
					result.add(method);
				}
			}
		}
	}

	public Iterable<? extends Method> getGetter() {
		return Iterables.filter(getMethods(), new Predicate<Method>() {
			@Override
			public boolean apply(Method input) {
				return input.getName().startsWith("get");
			}
		});
	}

	public boolean isAssignable(TypeMirror superClass) {
		return Environment.get().getTypeUtils().isAssignable(typeMirror, superClass);
	}

	public boolean isAssignable(Class<?> superClass) {
		final DeclaredType type = Environment.findType(superClass);
		return Environment.get().getTypeUtils().isAssignable(typeMirror, type);
	}

	public boolean isPrimitive() {
		return typeMirror.getKind().isPrimitive();
	}

	public Iterable<Field> getFields() {
		final List<Field> result = Lists.newArrayList();
		if (elt != null) {
			TypeElement superType = elt;
			while (superType != null && !"java.lang.Object".equals(superType.getSimpleName().toString())) {
				for (final Element element : superType.getEnclosedElements()) {
					if (element instanceof VariableElement) {
						// Get the field
						final VariableElement field = (VariableElement) element;
						if (field.getModifiers().contains(javax.lang.model.element.Modifier.STATIC))
							continue;

						result.add(new Field(field, field.asType()));
					}
				}

				superType = (TypeElement) Environment.get().getTypeUtils().asElement(superType.getSuperclass());
			}
		}

		return result;
	}

	public boolean isEnum() {
		return elt != null && elt.getKind() == ElementKind.ENUM;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Type)
			return typeMirror.equals(((Type) obj).typeMirror);
		return false;
	}
}
