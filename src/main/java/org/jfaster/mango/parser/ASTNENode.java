/*
 * Copyright 2014 mango.jfaster.org
 *
 * The Mango Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.jfaster.mango.parser;

import org.jfaster.mango.support.RuntimeContext;

public class ASTNENode extends AbstractExpression {

    public ASTNENode(int id) {
        super(id);
    }

    public ASTNENode(Parser p, int id) {
        super(p, id);
    }

    @Override
    public boolean evaluate(RuntimeContext context) {
        Object left = ((AbstractExpression) jjtGetChild(0)).value(context);
        Object right = ((AbstractExpression) jjtGetChild(1)).value(context);
        if (left instanceof Integer && right instanceof Integer) {
            return MathUtils.compare((Integer) left, (Integer) right) != 0;
        }

        if (left != null && right != null &&
                (left.getClass().isAssignableFrom(right.getClass()) ||
                        right.getClass().isAssignableFrom(left.getClass()))) {
            return !left.equals(right);
        }

        left = (left == null) ? null : left.toString();
        right = (right == null) ? null : right.toString();

        if (left == null && right == null) {
            return false;
        } else if (left == null || right == null) {
            return true;
        } else {
            return !left.equals(right);
        }
    }

    @Override
    public Object value(RuntimeContext context) {
        return evaluate(context) ? Boolean.TRUE : Boolean.FALSE;
    }

    @Override
    public Object jjtAccept(ParserVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }

}