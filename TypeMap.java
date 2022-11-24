import java.util.*;

public class TypeMap extends HashMap<Variable, Type> { 

// TypeMap is implemented as a Java HashMap.  
// Plus a 'display' method to facilitate experimentation.
    public void display() {
        // TODO Typemap 출력 포맷 변경
        System.out.print("{");
        for (Map.Entry<Variable, Type> entry : entrySet()) {
            System.out.print("<" + entry.getKey() + ", " + entry.getValue()+">, ");
        }
        System.out.println("}");
    }
}
