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


import java.io.IOException;
import org.wikidata.wdtk.examples.ExampleHelpers;
import org.wikidata.wdtk.examples.JsonSerializationProcessor;

/**
 *
 * @author m9ra
 */
public class DataExtractor {    
    public static final String PresidentId = "Q30461";
    
    public static final String HumanId = "Q5";
    
    public static final String CountryId = "Q6256";
    
    public static final String InstanceOfProperty = "P31";
    
    public static final String SubclassOfProperty = "P279";
    
    public static final String SpouseProperty="P26";
    
    public static final String BrotherProperty="P7";
    
    public static final String SisterProperty="P9";
    
    public static final String FatherProperty="P22";
    
    public static final String MotherProperty="P25";
    
    public static final String ChildProperty="P40";
    
    public static final String GenderProperty="P21";
    
    public static final String PositionHeldProperty="P39";
    
    public static final String CitizenShipProperty="P27";
    
    public static final String CapitalProperty="P36";
    
    public static final String ContinentProperty="P30";

    /**
     * Runs the example program.
     *
     * @param args
     * @throws IOException if there was a problem in writing the output file
     */
    public static void main(String[] args) throws IOException {
        ExampleHelpers.configureLogging();
        
        DiscoveringProcessor processor = new DiscoveringProcessor();
        //start topic discovery from president node
        processor.includeId(PresidentId);
        processor.allowEdge(InstanceOfProperty); 
        processor.allowEdge(SubclassOfProperty); //search only for president nodes        
        ExampleHelpers.processEntitiesFromWikidataDump(processor);
        processor.endLayer();        
        processor.resetFiltering();
        
        processor.allowEdge(PositionHeldProperty);
        ExampleHelpers.processEntitiesFromWikidataDump(processor);
        processor.endLayer();
        processor.resetFiltering();
        
        addPresidentDiscoverEdges(processor);
        ExampleHelpers.processEntitiesFromWikidataDump(processor);
        processor.endLayer();
        processor.resetFiltering();
        
        
     /*   PropertyFilter filter = new PropertyFilter(processor.getIds());
        initializeFilter(filter);
        ExampleHelpers.processEntitiesFromWikidataDump(filter);*/
        
        KnowledgeGraphWriter writer = new KnowledgeGraphWriter("test.db", processor.getIds());
        addExplicitOutputEdges(writer);
       // initializeFilter(writer);
        ExampleHelpers.processEntitiesFromWikidataDump(writer);
        writer.close();
    }
    
    private static void addPresidentDiscoverEdges(FilterableProcessor processor){
        String[] ids=new String[]{
            SpouseProperty,
            BrotherProperty,
            SisterProperty,
            MotherProperty,
            FatherProperty,
            ChildProperty,
            GenderProperty,
            CitizenShipProperty,
            CapitalProperty,
            ContinentProperty                
        };
        
        for(String id: ids){
            processor.allowEdge(id);
        }
    }
    
    private static void addExplicitOutputEdges(FilterableProcessor processor){
        String[] ids=new String[]{
            SubclassOfProperty,
            InstanceOfProperty,
            SpouseProperty,
            BrotherProperty,
            SisterProperty,
            MotherProperty,
            FatherProperty,
            ChildProperty,
            GenderProperty,
            PositionHeldProperty,
            CitizenShipProperty,
            CapitalProperty,
            ContinentProperty                
        };
        
        for(String id: ids){
            processor.allowEdge(id);
        }
    }
    
    private static void initializeFilter(FilterableProcessor processor) {
        processor.allowPropertyConnection(InstanceOfProperty, HumanId);
        processor.allowPropertyConnection(InstanceOfProperty, CountryId);
    }
    
}
