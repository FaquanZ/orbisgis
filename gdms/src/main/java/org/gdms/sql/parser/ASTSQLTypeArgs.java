/* Generated By:JJTree: Do not edit this line. ASTSQLTypeArgs.java Version 4.1 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package org.gdms.sql.parser;

public
class ASTSQLTypeArgs extends SimpleNode {
  public ASTSQLTypeArgs(int id) {
    super(id);
  }

  public ASTSQLTypeArgs(SQLEngine p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(SQLEngineVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=54da2a65330d62c932f1b7593de40f6e (do not edit this line) */