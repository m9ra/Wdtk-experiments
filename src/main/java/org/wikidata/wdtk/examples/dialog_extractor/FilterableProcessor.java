/*
 * Copyright 2015 Wikidata Toolkit Developers.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wikidata.wdtk.examples.dialog_extractor;

/*
 * #%L
 * Wikidata Toolkit Examples
 * %%
 * Copyright (C) 2014 - 2015 Wikidata Toolkit Developers
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
import java.util.HashMap;
import java.util.HashSet;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocumentProcessor;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue;
import org.wikidata.wdtk.datamodel.interfaces.PropertyDocument;
import org.wikidata.wdtk.datamodel.interfaces.Statement;
import org.wikidata.wdtk.datamodel.interfaces.StatementGroup;
import org.wikidata.wdtk.datamodel.interfaces.Value;
import org.wikidata.wdtk.datamodel.interfaces.ValueSnak;
import org.wikidata.wdtk.datamodel.interfaces.Snak;

/**
 *
 * @author m9ra
 */
public abstract class FilterableProcessor implements EntityDocumentProcessor {

    private final HashMap<String, HashSet<String>> requiredProperties = new HashMap<>();

    protected final HashSet<String> allowedIds = new HashSet<>();

    private final HashSet<String> allowedEdges = new HashSet<>();

    @Override
    public void processItemDocument(ItemDocument itemDocument) {
        boolean allowed = isAllowed(itemDocument);
        if (allowed) {
            acceptedItemDocument(itemDocument);
        }
    }

    @Override
    public void processPropertyDocument(PropertyDocument propertyDocument) {
        // we do not serialize any properties
    }

    public void allowPropertyConnection(String propertyId, String targetId) {
        allowedIds.add(targetId);
        if (!requiredProperties.containsKey(propertyId)) {
            requiredProperties.put(propertyId, new HashSet<String>());
        }

        HashSet<String> storage = requiredProperties.get(propertyId);
        storage.add(targetId);
    }

    public void allowEdge(String propertyId) {
        allowedEdges.add(propertyId);
    }
    
    public void resetFiltering(){
        allowedEdges.clear();
        requiredProperties.clear();
    }

    protected abstract void acceptedItemDocument(ItemDocument itemDocument);

    protected boolean isAllowed(Snak snak) {
        if (allowedEdges.size() == 0) {
            return true;
        }

        String propertyId = snak.getPropertyId().getId();
        return allowedEdges.contains(propertyId);
    }

    private boolean isAllowed(ItemDocument itemDocument) {
        if (allowedIds.size() == 0) //by default we allow everything
        {
            return true;
        }

        if (allowedIds.contains(itemDocument.getItemId().getId())) {
            return true;
        }

        for (StatementGroup sg : itemDocument.getStatementGroups()) {
            String propertyId = sg.getProperty().getId();
            if (!requiredProperties.containsKey(propertyId)) {
                continue;
            }

            HashSet<String> requiredTopics = requiredProperties.get(propertyId);
            for (Statement s : sg.getStatements()) {
                if (s.getClaim().getMainSnak() instanceof ValueSnak) {
                    Value v = ((ValueSnak) s.getClaim().getMainSnak())
                            .getValue();

                    if (v instanceof ItemIdValue) {
                        String idValue = ((ItemIdValue) v).getId();
                        if (requiredTopics.contains(idValue)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

}
