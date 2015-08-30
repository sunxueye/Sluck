package org.sluckframework.domain.identifier;


/**
 * 默认的标识符，UUID实现 String类型
 * 
 * @author sunxy
 * @time 2015年8月28日 上午11:42:11	
 * @since 1.0
 */
public class DefaultIdentifier implements Identifier<String> {

	private static final long serialVersionUID = 5562689869378044877L;

    private final String identifier;
	
	public DefaultIdentifier() {
		identifier = IdentifierFactory.getInstance().generateIdentifier();
	}
	
	public DefaultIdentifier(String identifier) {
		this.identifier = identifier;
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((identifier == null) ? 0 : identifier.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DefaultIdentifier other = (DefaultIdentifier) obj;
		if (identifier == null) {
			if (other.identifier != null)
				return false;
		} else if (!identifier.equals(other.identifier))
			return false;
		return true;
	}

}
