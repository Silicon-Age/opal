package com.opal.types;

import com.opal.AbstractTransactionAware;
import com.opal.Opal;

/* C = child opal (contained in the Set), P = parent opal (owner of the Set) */
public abstract class AbstractOpalBackCollectionSet<C extends Opal<?>, P extends Opal<?>> extends AbstractTransactionAware implements OpalBackCollectionSet<C, P> {
	/* FIXME: What can go here? */
}
