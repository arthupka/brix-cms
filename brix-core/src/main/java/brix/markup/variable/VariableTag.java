/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package brix.markup.variable;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import brix.Brix;
import brix.BrixNodeModel;
import brix.jcr.wrapper.BrixNode;
import brix.markup.tag.Tag;

public class VariableTag implements Tag, VariableKeyProvider
{
	private final Tag delegate;
	private final BrixNodeModel pageNodeModel;

	public VariableTag(BrixNode pageNode, Tag delegate)
	{
		this.pageNodeModel = new BrixNodeModel(pageNode);
		this.pageNodeModel.detach();
		this.delegate = delegate;
	}

	public Tag getDelegate()
	{
		return delegate;
	}

	private static final String ATTRIBUTE_PREFIX = Brix.NS_PREFIX + "var:";
 
	static String getKey(String value)
	{
		String key = null;
		if (value != null && value.startsWith(ATTRIBUTE_PREFIX))
		{
			key = value.substring(ATTRIBUTE_PREFIX.length());
		}
		if (key != null && key.length() == 0)
		{
			key = null;
		}
		return key;
	}

	public Map<String, String> getAttributeMap()
	{
		Map<String, String> original = getDelegate().getAttributeMap();
		BrixNode pageNode = new BrixNodeModel(pageNodeModel).getObject();
		if (pageNode instanceof VariableValueProvider)
		{
			VariableValueProvider variableValueProvider = (VariableValueProvider) pageNode;
			Map<String, String> result = new HashMap<String, String>();
			for (Entry<String, String> e : original.entrySet())
			{
				String k = getKey(e.getValue());
				if (k == null)
				{
					result.put(e.getKey(), e.getValue());
				}
				else
				{
					String value = variableValueProvider.getVariableValue(k);
					result.put(e.getKey(), value != null ? value : e.getValue());
				}
			}
			return result;
		}
		else
		{
			return original;
		}
	}

	public Collection<String> getVariableKeys()
	{
		Set<String> result = new HashSet<String>();
		Map<String, String> attributes = getDelegate().getAttributeMap();
		for (String s : attributes.values())
		{
			String k = getKey(s);
			if (k != null)
			{
				result.add(k);
			}
		}
		if (getDelegate() instanceof VariableKeyProvider)
		{
			Collection<String> keys = ((VariableKeyProvider) getDelegate()).getVariableKeys();
			if (keys != null)
			{
				result.addAll(keys);
			}
		}
		return result;
	}

	public String getName()
	{
		return delegate.getName();
	}

	public Type getType()
	{
		return delegate.getType();
	}

}
