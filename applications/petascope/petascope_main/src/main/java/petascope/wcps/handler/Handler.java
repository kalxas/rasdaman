/*
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU  General Public License for more details.
 *
 * You should have received a copy of the GNU  General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2022 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps.handler;

import java.util.ArrayList;
import java.util.List;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.wcps.result.VisitorResult;

/**
 * Abstract class for other WCPS handlers.
 * Each handler has:
 * - A parent (root handler doesn't have parent) 
 * - A list of children handlers (if a handler is terminal, then it has no children)
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
public abstract class Handler {
    
    private Handler parent;
    private List<Handler> children;
    
    public Handler() {
        
    }
    
    public Handler(List<Handler> childHandlers) {
        
        if (childHandlers != null) {
            this.children = new ArrayList<>();
            for (Handler handler : childHandlers) {
                if (handler != null) {
                    this.children.add(handler);
                }
            }
        }
        
        if (childHandlers != null) {
            for (Handler childHandler : childHandlers) {
                if (childHandler != null) {
                    childHandler.setParent(this);
                }
            }
        }
    }
    
//    /**
//     * NOTE: cannot use constructor directly, it has this below problem with Spring.
//     * Error creating bean with name: Requested bean is currently in creation.
//     */
//    public static Handler create(Handler self, List<Handler> children) {
//        Handler obj = new Handler(self, children);
//        return obj;
//    }
    
    public Handler getParent() {
        return parent;
    }

    public void setParent(Handler parent) {
        this.parent = parent;
    }
    
    public void setChildren(List<Handler> handlers) {
        if (handlers != null) {
            this.children = new ArrayList<>();
            for (Handler handler : handlers) {
                this.children.add(handler);
                
                if (handler != null) {
                    handler.setParent(this);
                }
            }
        }
    }

    public List<Handler> getChildren() {
        return children;
    }
    
    public Handler getFirstChild() throws PetascopeException {
        if (children.size() < 1) {
            throw new PetascopeException(ExceptionCode.InternalComponentError, 
                    "Handler: " + this.getClass().getSimpleName() + " has not enough children to get the first child handler.");
        }
        return children.get(0);
    }
    
    public Handler getSecondChild() throws PetascopeException {
        if (children.size() < 2) {
            throw new PetascopeException(ExceptionCode.InternalComponentError, 
                    "Handler: " + this.getClass().getSimpleName() + " has not enough children to get the second child handler.");
        }        
        return children.get(1);
    }
    
    public Handler getThirdChild() throws PetascopeException {
        if (children.size() < 3) {
            throw new PetascopeException(ExceptionCode.InternalComponentError, 
                    "Handler: " + this.getClass().getSimpleName() + " has not enough children to get the thrid child handler.");
        }  
        return children.get(2);
    }
    
    public Handler getFourthChild() throws PetascopeException {
        if (children.size() < 4) {
            throw new PetascopeException(ExceptionCode.InternalComponentError, 
                    "Handler: " + this.getClass().getSimpleName() + " has not enough children to get the fourth child handler.");
        }  
        return children.get(3);
    }
    
    public Handler getFifthChild() throws PetascopeException {
        if (children.size() < 5) {
            throw new PetascopeException(ExceptionCode.InternalComponentError, 
                    "Handler: " + this.getClass().getSimpleName() + " has not enough children to get the fifth child handler.");
        }  
        return children.get(4);
    }
    
    public Handler getSixthChild() throws PetascopeException {
        if (children.size() < 6) {
            throw new PetascopeException(ExceptionCode.InternalComponentError, 
                    "Handler: " + this.getClass().getSimpleName() + " has not enough children to get the sixth child handler.");
        }  
        return children.get(5);
    }
    
    public Handler getSeventhChild() throws PetascopeException {
        if (children.size() < 7) {
            throw new PetascopeException(ExceptionCode.InternalComponentError, 
                    "Handler: " + this.getClass().getSimpleName() + " has not enough children to get the seventh child handler.");
        }  
        return children.get(6);
    }    
    
    public abstract VisitorResult handle() throws PetascopeException;

}
