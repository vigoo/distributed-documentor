package hu.distributeddocumentor.prefs;

import com.google.common.collect.Lists;
import hu.distributeddocumentor.model.Conditions;
import java.util.LinkedList;
import java.util.List;
import org.junit.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertTrue;

public class DocumentorPreferencesTest {

    @Test
    public void conditionalsRecognizedFromCommandLine() {
                
        String[] args = new String[] {
            "-D cond1",
            "-Dcond2",
            "-D  cond3"
        };
        DocumentorPreferences prefs = new DocumentorPreferences(args);
        Conditions conds = prefs.getConditions();
        
        assertEquals(3, conds.getEnabledConditionsCount());
        assertTrue(conds.allEnabled(conditions("cond1", "cond2", "cond3")));
    }
    
    @Test
    public void noConditionsByDefault() {
        String[] args = new String[0];
        DocumentorPreferences prefs = new DocumentorPreferences(args);
        Conditions conds = prefs.getConditions();
        
        assertEquals(0, conds.getEnabledConditionsCount());        
    }
    
    private List<String> conditions(String... conds) {
        return Lists.newArrayList(conds);
    }
}
