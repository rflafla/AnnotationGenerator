package fr.flafla.generator.access;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.SimpleTypeVisitor6;

public class TypeSimplifier extends SimpleTypeVisitor6<TypeMirror, Environment> {

	public static TypeMirror simplify(TypeMirror toBox, boolean boxPrimitives, Environment state) {
		if (toBox == null) {
			return null;
		}
		return toBox.accept(new TypeSimplifier(boxPrimitives), state);
	}

	// TODO use java 8 IntersectionType directly (jdk8 required)
	private static Class<?> intersectionClass;
	static {
		try {
			intersectionClass = Class.forName("javax.lang.model.type.IntersectionType");
		} catch (ClassNotFoundException e) {
		}
	}

	private static boolean isIntersection(TypeMirror type) {
		return intersectionClass != null && intersectionClass.isAssignableFrom(type.getClass());
	}

	private final boolean boxPrimitives;

	private TypeSimplifier(boolean boxPrimitives) {
		this.boxPrimitives = boxPrimitives;
	}

	@Override
	public TypeMirror visitDeclared(DeclaredType x, Environment state) {
		if (x.getTypeArguments().isEmpty()) {
			return x;
		}
		List<TypeMirror> newArgs = new ArrayList<TypeMirror>(x.getTypeArguments().size());
		for (TypeMirror original : x.getTypeArguments()) {
			// Are we looking at a self-parameterized type like Foo<T extends Foo<T>>?
			if (original.getKind().equals(TypeKind.TYPEVAR) && Environment.get().getTypeUtils().isAssignable(original, x)) {
				// If so, return a raw type
				return Environment.get().getTypeUtils().getDeclaredType((TypeElement) x.asElement());
			} else {
				newArgs.add(original.accept(this, state));
			}
		}
		return Environment.get().getTypeUtils().getDeclaredType((TypeElement) x.asElement(), newArgs
				.toArray(new TypeMirror[newArgs.size()]));
	}

	@Override
	public TypeMirror visitNoType(NoType x, Environment state) {
		if (boxPrimitives) {
			return Environment.findType(Void.class);
		}
		return x;
	}

	@Override
	public TypeMirror visitPrimitive(PrimitiveType x, Environment state) {
		if (boxPrimitives) {
			return Environment.get().getTypeUtils().boxedClass(x).asType();
		}
		return x;
	}

	@Override
	public TypeMirror visitTypeVariable(TypeVariable x, Environment state) {
		if (x.equals(x.getUpperBound())) {
			// See comment in TransportableTypeVisitor
			return Environment.get().getTypeUtils().erasure(x);
		}
		if (isIntersection(x.getUpperBound())) {
			return Environment.get().getTypeUtils().erasure(x);
		}
		return x.getUpperBound().accept(this, state);
	}

	@Override
	public TypeMirror visitWildcard(WildcardType x, Environment state) {
		return Environment.get().getTypeUtils().erasure(x).accept(this, state);
	}

	@Override
	protected TypeMirror defaultAction(TypeMirror x, Environment state) {
		return Environment.get().getTypeUtils().getNoType(TypeKind.NONE);
	}
}
