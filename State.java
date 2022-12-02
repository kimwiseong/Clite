import java.util.*;

public class State extends HashMap<Variable, Value> { 
    // Defines the set of variables and their associated values 
    // that are active during interpretation
    
    public State( ) { }
    
    public State(Variable key, Value val) {
        put(key, val);
    }
    
    public State onion(Variable key, Value val) {
        put(key, val); // Variable을 key로 Value value로 테이블에 저장
        // student exercise
        return this;
    }

    void display() {
        /** 저장된 state 출력 */
        int i = 0;
        System.out.print("{ ");
        for (Entry<Variable, Value> entry : entrySet()) {
            System.out.print("<" + entry.getKey() + ", " + entry.getValue()+">");
            if (i < entrySet().size()-1) System.out.print(", ");
            i++;
        }
        System.out.println(" }\n");
        // student exercise
   	}
}
