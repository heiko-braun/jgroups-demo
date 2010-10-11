/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jgroups.samples.client;

import com.google.gwt.core.client.EntryPoint;
import org.gwt.mosaic.ui.client.CaptionLayoutPanel;
import org.gwt.mosaic.ui.client.StackLayoutPanel;
import org.gwt.mosaic.ui.client.WindowPanel;
import org.gwt.mosaic.ui.client.layout.*;

/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class Bootstrap implements EntryPoint
{
    private StackLayoutPanel stack;

    public void onModuleLoad() {

        WindowPanel window = new WindowPanel("JGroups Chat");
        window.setAnimationEnabled(true);        
        // ------------------

        stack = new StackLayoutPanel();
        stack.setAnimationEnabled(true);


        stack.add(new UserPanel(), "Users");
        stack.add(new LayoutPanel(), "Cluster");

        final LayoutPanel lefthand = new LayoutPanel(new BoxLayout(BoxLayout.Orientation.VERTICAL));
        lefthand.setPadding(0);
        lefthand.add(stack, new BoxLayoutData(BoxLayoutData.FillStyle.BOTH, true));

        stack.showStack(0);
        
        // ------------------
        LayoutPanel main = new LayoutPanel(new BorderLayout());
        main.setPadding(2);
        main.add(new ChatClient());
        main.add(lefthand, new BorderLayoutData(BorderLayout.Region.WEST, "180px"));
        window.setWidget(main);

        // ------------------
        window.setAnimationEnabled(true);
        window.setSize("800px", "600px");
        window.center();
    }
}
