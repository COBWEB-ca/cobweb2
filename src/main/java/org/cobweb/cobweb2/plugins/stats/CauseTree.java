package org.cobweb.cobweb2.plugins.stats;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.cobweb.cobweb2.core.Cause;
import org.cobweb.cobweb2.plugins.stats.CauseTree.CauseTreeNode;
import org.reflections.Reflections;

public class CauseTree implements Iterable<CauseTreeNode> {

	public CauseTreeNode root = new CauseTreeNode(null, Cause.class);

	public CauseTree() {
		Reflections reflections = new Reflections("org.cobweb.cobweb2");
		Set<Class<? extends Cause>> causeTypes = reflections.getSubTypesOf(Cause.class);

		for (Class<? extends Cause> ct : causeTypes) {
			if (ct.isInterface() || Modifier.isAbstract(ct.getModifiers()))
				continue;

			try {
				Cause instance = ct.newInstance();
				CauseTreeNode thisNode = new CauseTreeNode(instance, ct);
				root.addChild(thisNode);
			} catch (InstantiationException | IllegalAccessException ex) {
				throw new RuntimeException(ex);
			}
		}
	}

	@Override
	public Iterator<CauseTreeNode> iterator() {
		return new Iterator<CauseTree.CauseTreeNode>() {

			Queue<CauseTreeNode> todo = new LinkedList<>();

			{
				todo.add(root);
			}

			@Override
			public boolean hasNext() {
				return !todo.isEmpty();
			}

			@Override
			public CauseTreeNode next() {
				CauseTreeNode result = todo.remove();
				todo.addAll(result.children);
				return result;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

		};
	}

	public static class CauseTreeNode implements Comparable<CauseTreeNode> {
		public Class<? extends Cause> type;
		public Cause cause;
		public CauseTreeNode parent;
		public List<CauseTreeNode> children = new ArrayList<>();

		public CauseTreeNode(Cause cause, Class<? extends Cause> type) {
			this.cause = cause;
			this.type = type;
		}

		public boolean accepts(CauseTreeNode n) {
			return type.isAssignableFrom(n.type);
		}

		public void addChild(CauseTreeNode n) {
			for (CauseTreeNode child : children) {
				if (child.accepts(n)) {
					child.addChild(n);
					return;
				}
			}
			Iterator<CauseTreeNode> iterator = children.iterator();
			while (iterator.hasNext()) {
				CauseTreeNode child = iterator.next();
				if (n.accepts(child)) {
					n.addChild(child);
					iterator.remove();
				}
			}
			children.add(n);
			Collections.sort(children);
			n.parent = this;
		}

		public String getName() {
			if (cause == null)
				return "Everything";
			else
				return cause.getName();
		}

		@Override
		public String toString() {
			String res = getName();

			if (!children.isEmpty()) {
				res += "{ ";
				for (CauseTreeNode causeTreeNode : children) {
					res += causeTreeNode.toString() + ", ";
				}
				res += " }";
			}
			return res;
		}

		@Override
		public int compareTo(CauseTreeNode o) {
			if (cause == null && o.cause == null)
				return 0;
			if (cause == null)
				return 1;
			if (o.cause == null)
				return -1;
			return cause.getName().compareTo(o.cause.getName());
		}
	}

}
