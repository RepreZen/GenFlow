package com.reprezen.genflow.api.normal.openapi;

import java.util.Set;

import com.google.common.collect.Sets;

/**
 * This class detects recursive references to avoid inifinite inlining attempts.
 * <p>
 * Whenever a reference is inlined, its canonical reference should be "visited"
 * around the code that then processes the inlined structure. This is easily
 * done by allocating a new Visit object using
 * {@link RefVisitor#visit(Reference) in a try-with-resource block. The Visit
 * object will then be automatically closed (terminating the visit upon exiting
 * the block.
 * <p>
 * Attempting to visit a recursive reference will throw a
 * VisitRecursionException.
 */
public class RefVisitor {

	private final Set<Reference> visitingRefs = Sets.newHashSet();

	public Visit visit(Reference ref) throws VisitRecursionException {
		return new Visit(ref);
	}

	public class Visit implements AutoCloseable {
		private final Reference ref;

		public Visit(Reference ref) throws VisitRecursionException {
			if (visitingRefs.contains(ref)) {
				throw new VisitRecursionException();
			} else {
				this.ref = ref;
				visitingRefs.add(ref);
			}
		}

		@Override
		public void close() {
			visitingRefs.remove(ref);
		}
	}

	public class VisitRecursionException extends Exception {
		private static final long serialVersionUID = 1L;
	}
}
