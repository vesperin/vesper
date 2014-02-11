package edu.ucsc.refactor.internal;

import com.google.common.collect.Lists;
import edu.ucsc.refactor.Context;
import edu.ucsc.refactor.NamedLocation;
import edu.ucsc.refactor.util.Locations;
import org.eclipse.jdt.core.dom.ASTNode;
import se.fishtank.css.selectors.Selector;
import se.fishtank.css.selectors.scanner.Scanner;
import se.fishtank.css.selectors.scanner.ScannerException;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class MatchedUnit extends AbstractProgramUnit {
    /**
     * Construct a {@code MatchedUnit}, which is responsible for
     * matching nodes within the node's subtree.
     *
     * @param selectors A group of selectors to query.
     */
    public MatchedUnit(String selectors){
        super(selectors);
    }

    @Override public List<NamedLocation> getLocations(Context context) {
        final List<NamedLocation> locations = Lists.newArrayList();

        final Set<ASTNode> result = querySelectorAll(getName());

        for(ASTNode each : result){
            locations.add(new ProgramUnitLocation(each, Locations.locate(each)));
        }



        return locations;
    }



    public Set<ASTNode> querySelectorAll(String selectors) throws RuntimeException {
        List<List<Object>> groups;
        try {
            groups = parse(selectors);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        final Set<ASTNode> results = new LinkedHashSet<ASTNode>();

        for (List<Object> parts : groups) {
            Set<ASTNode> result = check(parts);
            if (!result.isEmpty()) {
                results.addAll(result);
            }
        }

        return results;
    }

    private List<List<Object>> parse(String selectors) throws RuntimeException {
        throw new UnsupportedOperationException();
    }

    private Set<ASTNode> check(List<Object> parts) throws RuntimeException {
        throw new UnsupportedOperationException();
    }

    public static void main(String[] args) throws ScannerException {
        final Scanner scanner = new Scanner("[^attr]");
        final List<List<Selector>> a = scanner.scan();
        a.isEmpty();
    }


}
