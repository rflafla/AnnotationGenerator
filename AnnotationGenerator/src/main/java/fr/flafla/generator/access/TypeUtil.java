package fr.flafla.generator.access;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.SimpleTypeVisitor6;

public class TypeUtil {
	public static class TypeNameVisitor extends SimpleTypeVisitor6<String, Environment> {
		private final boolean complete;

		private final DeclaredType from;

		public TypeNameVisitor(boolean complete, DeclaredType from) {
			this.complete = complete;
			this.from = from;
		}
		
		@Override
		public String visitArray(ArrayType t, Environment p) {
			final TypeMirror componentType = t.getComponentType();
			return getTypeName(from, componentType)+"[]";
		}
		
		@Override
		public String visitPrimitive(PrimitiveType t, Environment p) {
			return t.getKind().name().toLowerCase();
		}
		
		@Override
		public String visitDeclared(DeclaredType t, Environment p) {
			return complete?getCompleteName(t):getName(t);
		}
		
		@Override
		public String visitTypeVariable(TypeVariable t, Environment p) {
			System.out.println(from.asElement().getSimpleName());
			final TypeMirror capture = Environment.get().getTypeUtils().asMemberOf(from, t.asElement());
			return complete?getCompleteName(capture):getName(capture);
		}
		
		@Override
		protected String defaultAction(TypeMirror e, Environment p) {
			return e.getKind().name().toLowerCase();
		}
	}
	
	public static String getTypeName(DeclaredType enclosingType, TypeMirror type) {
		return new TypeNameVisitor(true, enclosingType).visit(type);
	}
	
	public static String getTypeNameRaw(DeclaredType enclosingType, TypeMirror type) {
		return new TypeNameVisitor(false, enclosingType).visit(type);
	}
	
	public static String getName(TypeMirror typeMirror) {
		try {
			final DeclaredType simplify = (DeclaredType) TypeSimplifier.simplify(typeMirror, false, null);
			
			final TypeElement type = (TypeElement) Environment.get().getTypeUtils().asElement(simplify);
			return type.getQualifiedName().toString();
		} catch (final RuntimeException e) {
			e.printStackTrace();
			throw e;
		}
	}
	public static String getCompleteName(TypeMirror typeMirror) {
		try {
			final DeclaredType simplify = (DeclaredType) TypeSimplifier.simplify(typeMirror, false, null);
			
			final TypeElement type = (TypeElement) Environment.get().getTypeUtils().asElement(simplify);
			final String qualifiedName = type.getQualifiedName().toString();
			
			final List<? extends TypeMirror> args = simplify.getTypeArguments();
			if (args.size() > 0) {
				final StringBuilder sb = new StringBuilder();
				sb.append(qualifiedName).append("<");
				boolean notFirst = false;
				for (final TypeMirror arg : args) {
					if (notFirst) {
						sb.append(',');
					}
					notFirst = true;
					final Element argElt = Environment.asElement(arg);
					sb.append(argElt);
				}
				sb.append(">");
				return sb.toString();
			}
			return qualifiedName;
		} catch (final RuntimeException e) {
			e.printStackTrace();
			throw e;
		}
	}
}
