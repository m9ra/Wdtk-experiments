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


import java.util.Collection;
import java.util.HashSet;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;

/**
 *
 * @author m9ra
 */
public class PropertyFilter extends FilterableProcessor {

    private final HashSet<String> includedDocuments = new HashSet<String>();

    private final HashSet<String> acceptedDocuments = new HashSet<>();
    
    public PropertyFilter(Collection<String> ids){
        includedDocuments.addAll(ids);
    }
    
    @Override
    public void processItemDocument(ItemDocument itemDocument) {
        if (includedDocuments.contains(itemDocument.getItemId().getId())) {
            super.processItemDocument(itemDocument);
        }
    }

    @Override
    protected void acceptedItemDocument(ItemDocument itemDocument) {
        acceptedDocuments.add(itemDocument.getItemId().getId());
    }

    protected HashSet<String> getIds(){
        return acceptedDocuments;
    }
}
