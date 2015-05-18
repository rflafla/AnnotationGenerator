package fr.flafla.generator.access;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class Type extends AbstractElement<TypeElement> {
	private final TypeMirror typeMirror;
	private final String sourcePackage;
	private final String qualifiedName;
	private final String name;

	public Type(TypeMirror type) {
		super((TypeElement) Environment.get().getTypeUtils().asElement(type));
		this.typeMirror = type;
		qualifiedName = this.elt.getQualifiedName().toString();
		final int lastPoint = qualifiedName.lastIndexOf('.');
		sourcePackage = qualifiedName.substring(0, lastPoint);
		name = qualifiedName.substring(lastPoint + 1);
	}

	public Type(Class<?> clazz) {
		this(Environment.get().getElementUtils().getTypeElement(clazz.getName()).asType());
	}

	public Type(Type type) {
		this(type.typeMirror);
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
	
	public Iterable<? extends Type> getInterfaces() {
		final List<? extends TypeMirror> interfaces = elt.getInterfaces();
		final List<Type> result = new ArrayList<Type>();
		for (final TypeMirror intf : interfaces) {
			result.add(new Type(intf));
		}
		return result;
	}
	
	public Iterable<? extends Method> getMethods() {
		final Set<String> names = new HashSet<String>();
		final List<Method> result = new ArrayList<Method>();
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

		return result;
	}
	
	public Iterable<? extends Method> getTypeMethods() {
		final Set<String> names = new HashSet<String>();
		final List<Method> result = new ArrayList<Method>();
		extractMethods(names, result, elt);
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
	
	public Iterable<Field> getFields() {
		final List<Field> result = Lists.newArrayList();
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
		
		return result;
	}

	public boolean isEnum() {
		return elt.getKind() == ElementKind.ENUM;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Type)
			return typeMirror.equals(((Type) obj).typeMirror);
		return false;
	}
}
