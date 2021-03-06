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

package brix.markup;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;

import brix.jcr.wrapper.BrixNode;
import brix.markup.tag.ComponentTag;
import brix.markup.tag.Tag;
import brix.plugin.site.SitePlugin;
import brix.web.generic.IGenericComponent;

/**
 * Helper class for components with custom node markup. This class takes care of
 * generating the markup and creating/removing of appropriate wicket components.
 * 
 * @author Matej Knopp
 */
public class MarkupHelper implements Serializable
{
	public MarkupHelper(IGenericComponent<BrixNode> component)
	{
		this.component = component;
		initMarkup();
	}

	private final IGenericComponent<BrixNode> component;
	
	/**
	 * Each tag component ids prefixed by this.
	 */
	private final static String COMPONENT_PREFIX = "brix-";

	public static String getComponentID(ComponentTag tag)
	{
		String uid = tag.getUniqueTagId();
		return COMPONENT_PREFIX + (uid != null ? uid.toString() : "");
	}

	private String markup = null;

	public String getMarkup()
	{
		return markup;
	}

	private MarkupCache getMarkupCache()
	{
		return SitePlugin.get().getMarkupCache();
	}
	
	/**
	 * Returns existing tag components on page.
	 * @return
	 */
	private Set<String> getExistingComponents()
	{
		Set<String> result = new HashSet<String>();
		Iterator<? extends Component> i = ((MarkupContainer)component).iterator();
		while (i.hasNext())
		{
			Component c = i.next();
			if (c.getId().startsWith(COMPONENT_PREFIX))
			{
				result.add(c.getId());
			}
		}
		return result;
	}

	/**
	 * Generates the markup and makes sure that the markup container contains
	 * all components generated by the {@link ComponentTag}s in markup.
	 * Also removes components no longer present in markup.
	 */
	private void initMarkup()
	{
		final Set<String> existingComponents = getExistingComponents();
		final Set<String> components = new HashSet<String>();
		GeneratedMarkup markup = getMarkupCache().getMarkup(component);
		
		MarkupRenderer renderer = new MarkupRenderer(markup.items, markup.doctype)
		{
			@Override
			void postprocessTagAttributes(Tag tag, Map<String, String> attributes)
			{
				// if during rendering we have a ComponentTag
				if (tag instanceof ComponentTag && tag.getType() != Tag.Type.CLOSE)
				{
					ComponentTag componentTag = (ComponentTag) tag;
					String id = getComponentID(componentTag);
					
					// check if the component already is in hierarchy
					if (existingComponents.contains(id))
					{
						// just put the wicket:id attribute to component tag
						attributes.put("wicket:id", id);
						components.add(id);
					}
					else
					{
						// otherwise we need to create the component instance
						Component c = componentTag.getComponent(id, component.getModel());
						if (c != null)
						{
							attributes.put("wicket:id", id);
							components.add(id);
							((MarkupContainer)component).add(c);
						}
					}
				}
			}
		};
		this.markup = renderer.render();

		// go through existing components and remove those not present in
		// current markup
		for (String s : existingComponents)
		{
			if (!components.contains(s))
			{
				((MarkupContainer)component).get(s).remove();
			}
		}
	}

}
