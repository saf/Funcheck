package checkers.func;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * A tool for Prolog clauses output.
 */
public class FactPrinter {

    /**
     * Representation of a Prolog functor. 
     */
    protected class Functor {
        public String name;
        public Integer arity;

        public Functor(String name, Integer arity) {
            this.name = name;
            this.arity = arity;
        }

        @Override
        public boolean equals(Object o) {
            if (o != null && o instanceof Functor) {
                Functor f = (Functor) o;
                return f.name.equals(this.name) && f.arity == this.arity;
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return name.hashCode() + arity;
        }
    }

    protected final static String factsFilename = "program.pl";

    /**
     * Collection of functors that were declared discontiguous.
     */
    protected Collection<Functor> discontiguousFunctors;
    
    protected BufferedWriter factFile;

    public FactPrinter() throws IOException {
        factFile = new BufferedWriter(new FileWriter(factsFilename));
        discontiguousFunctors = new HashSet<Functor>();
        addCommand("dynamic", new Functor("pure", 1));
    }

    /**
     * Returns a Prolog representation of an object.
     */
    protected String representation(Object o) {
        if (o instanceof Functor) {
            Functor f = (Functor) o;
            return String.format("%s/%d", f.name, f.arity);
        } else if (o instanceof String) {
            return "'" + (String) o + "'";
        } else {
            return "'" + o.toString() + "'";
        }
    }

    /**
     * 
     * @param s An ordered collection of strings to be joined
     * @param d The delimiter
     * @return The string resulting from concatenating all given strings
     *         and inserting the delimiter between them.
     */
    protected String join(Collection<String> s, String d) {
        StringBuilder b = new StringBuilder();
        Iterator it = s.iterator();
        while (it.hasNext()) {
            b.append(it.next());
            if (it.hasNext()) {
                b.append(d);
            }
        };
        return b.toString();
    }

    /**
     * Writes a string to the facts file as a single line, checking for errors.
     *
     * @param s The string to be printed
     */
    protected void writeLine(String s) {
        System.err.println("-> facts: " + s);
        try {
            factFile.write(s + "\n");
            factFile.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns a predicate with the given name and arguments.
     */
    protected String predicate(String name, Object... args) {
        List<String> argumentStrings = new LinkedList<String>();
        for (Object o : args) {
            argumentStrings.add(representation(o));
        };
        String argumentString = join(argumentStrings, ", ");
        return String.format("%s(%s)", name, argumentString);
    }

    /**
     * Prints a command to the fact file.
     */
    protected void addCommand(String name, Object... args) {
        String predicate = predicate(name, args);
        writeLine(String.format(":- %s.", predicate));
    }

    /**
     * Prints a clause to the fact file.
     *
     * @param clause The name of the fact's top-level functor.
     * @param args The arguments of the fact. 
     */
    protected void addFact(String name, Object... args) {
        /* Check if discontiguous/1 needs to be printed first */
        Functor f = new Functor(name, args.length);
        if (!discontiguousFunctors.contains(f)) {
            addCommand("discontiguous", f);
            discontiguousFunctors.add(f);
        };
        String predicate = predicate(name, args);
        writeLine(String.format("%s.", predicate));
    }

    public void addClass(String name) {
        addFact("class", name);
    }

    public void addMethod(String className, String methodName) {
        addFact("method", className, methodName);
    }

    public void addPureDeclaration(String methodName) {
        addFact("pure", methodName);
    }

    public void addMethodCall(String key, String caller, String callee) {
        addFact("calls", key, caller, callee);
    }
}
