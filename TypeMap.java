import java.util.*;

public class TypeMap extends HashMap<Variable, Type> {

// TypeMap is implemented as a Java HashMap.  
// Plus a 'display' method to facilitate experimentation.
    public void display() {
        int i = 0;
        System.out.print("{ ");
        for (Map.Entry<Variable, Type> entry : entrySet()) {
            System.out.print("<" + entry.getKey() + ", " + entry.getValue()+">");
            if (i < entrySet().size()-1) System.out.print(", ");
            i++;
        }
        System.out.println(" }\n");
    }
}
