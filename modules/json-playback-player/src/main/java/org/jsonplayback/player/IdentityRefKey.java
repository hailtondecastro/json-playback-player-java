package org.jsonplayback.player;

public class IdentityRefKey {
	private Object ref;
	
	public IdentityRefKey(Object ref) {
super();
		this.ref = ref;
	}

	public Object getRef() {
		return ref;
	}
	
	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return System.identityHashCode(this.ref);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IdentityRefKey) {
			return this.ref == ((IdentityRefKey)obj).ref;			
		} else {
			return false;
		}
			
	}
}
/*gerando conflito*/