package nl.utwente.processing.pmd;

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;

public class AbstractProcessingRule extends AbstractJavaRule {

	public void addViolationWithMessage(Object data, Node node, String msg, int beginLine, int endLine, Object[] args) {
		RuleContext ruleContext = (RuleContext) data;
		ruleContext.getLanguageVersion().getLanguageVersionHandler().getRuleViolationFactory().addViolation(ruleContext, this,
				node, msg, beginLine, endLine, args);
	}
}
