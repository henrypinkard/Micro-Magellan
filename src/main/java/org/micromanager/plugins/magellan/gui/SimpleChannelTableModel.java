///////////////////////////////////////////////////////////////////////////////
// AUTHOR:       Henry Pinkard, henry.pinkard@gmail.com
//
// COPYRIGHT:    University of California, San Francisco, 2015
//
// LICENSE:      This file is distributed under the BSD license.
//               License text is included with the source distribution.
//
//               This file is distributed in the hope that it will be useful,
//               but WITHOUT ANY WARRANTY; without even the implied warranty
//               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.
//

package main.java.org.micromanager.plugins.magellan.gui;

import com.google.common.eventbus.Subscribe;
import java.awt.Color;
import java.util.ArrayList;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import main.java.org.micromanager.plugins.magellan.channels.ChannelSpec;
import main.java.org.micromanager.plugins.magellan.main.Magellan;
import main.java.org.micromanager.plugins.magellan.misc.GlobalSettings;
import mmcorej.CMMCore;
import org.micromanager.events.ExposureChangedEvent;
import main.java.org.micromanager.plugins.magellan.demo.DemoModeImageData;
import main.java.org.micromanager.plugins.magellan.misc.NumberUtils;

/**
 *
 * @author Henry
 */
public class SimpleChannelTableModel extends AbstractTableModel implements TableModelListener {

   
      private ChannelSpec channels_;
      private final CMMCore core_;
      private final boolean exploreTable_;
      private boolean selectAll_ = true;
      public final String[] COLUMN_NAMES = new String[]{
         "Use",
         "Configuration",
         "Exposure",
         "Z-offset (um)",
         "Color",         
   };
      
      
   public SimpleChannelTableModel(ChannelSpec channels, boolean showColor) {
      exploreTable_ = !showColor;
      core_ = Magellan.getCore();   
      channels_ = channels;
      Magellan.getStudio().getEventManager().registerForEvents(this);
   }

   public void selectAllChannels() {
       //Alternately select all channels or deselect channels
       channels_.setUseOnAll(selectAll_);
       selectAll_ = !selectAll_;
       fireTableDataChanged();
   }

   public void synchronizeExposures() {
       //Alternately select all channels or deselect channels
       channels_.synchronizeExposures();
       fireTableDataChanged();
   }
   
   public void shutdown() {
      Magellan.getStudio().getEventManager().unregisterForEvents(this);
   }
   
   public boolean anyChannelsActive() {
      return channels_ == null ? false : channels_.anyActive();
   }

   public void setChannelGroup(String group) {
      if (channels_ != null) {
          channels_.updateChannelGroup(group);
      }
   }
   
   public void setChannels(ChannelSpec channels) {
      channels_ = channels;
   }
   
   public String[] getAllChannelNames() {  
       return channels_ == null ? new String[]{} : channels_.getAllChannelNames();
   }
      
   public String[] getActiveChannelNames() {
      return channels_ == null ? new String[]{} : channels_.getActiveChannelNames();
   }

   @Override
   public int getRowCount() {
      if (channels_ == null) {
         return 0;
      } else {
         return channels_.getNumChannels();
      }
   }

   @Override
   public int getColumnCount() {
      return COLUMN_NAMES.length - (exploreTable_ ? 1 : 0);
   }

   @Override
   public String getColumnName(int columnIndex) {
      return COLUMN_NAMES[columnIndex];
   }

   @Override
   public Object getValueAt(int rowIndex, int columnIndex) {
            //use name exposure, color
      if (columnIndex == 0) {
         return channels_.getChannelSetting(rowIndex).getUse();
      } else if (columnIndex == 1) {
         return channels_.getChannelSetting(rowIndex).name_;
      } else if (columnIndex == 2) {
         return channels_.getChannelSetting(rowIndex).exposure_;
      } else if (columnIndex == 3) {         
        return channels_.getChannelSetting(rowIndex).offset_; 
      } else {
          return channels_.getChannelSetting(rowIndex).color_;             
      }
   }

   @Override
   public Class getColumnClass(int columnIndex) {
      if (columnIndex == 0) {
         return Boolean.class;
      } else if (columnIndex == 1) {
         return String.class;
      } else if (columnIndex == 2) {
         return Double.class;
      } else if (columnIndex == 3) {
         return Double.class;
      } else {
         return Color.class;
      }
   }

   @Override
   public void setValueAt(Object value, int row, int columnIndex) {
      //use name exposure, color  
      int numCamChannels = (int) core_.getNumberOfCameraChannels();
      
      if (columnIndex == 0) {                   
         channels_.getChannelSetting(row).use_ = ((Boolean) value);
         //same for all other channels of the same camera_
         if (numCamChannels > 1) {
            for (int i = (row - row % numCamChannels); i < (row /numCamChannels + 1) * numCamChannels;i++ ) {
               channels_.getChannelSetting(i).use_ = ((Boolean) value);
            }
            fireTableDataChanged();
         }
      } else if (columnIndex == 1) {       
         //cant edit channel name
      } else if (columnIndex == 2) {
         channels_.getChannelSetting(row).exposure_ = ((Double) value);
         //same for all other channels of the same camera_
         if (numCamChannels > 1) {
            for (int i = (row - row % numCamChannels); i < (row / numCamChannels + 1) * numCamChannels; i++) {
               channels_.getChannelSetting(i).exposure_ = ((Double) value);
            }
            fireTableDataChanged();
         }
      } else if (columnIndex == 3) {
          channels_.getChannelSetting(row).offset_ = ((Double) value);
      } else {
         channels_.getChannelSetting(row).color_ = ((Color) value);
      }
      //Store the newly selected value in preferences
      channels_.storeCurrentSettingsInPrefs();
   }

   @Override
   public boolean isCellEditable(int nRow, int nCol) {
      return nCol != 1;
   }

   @Override
   public void tableChanged(TableModelEvent e) {
   }

    @Subscribe
    public void onExposureChanged(ExposureChangedEvent event) {
        for (int i = 0; i < channels_.getNumChannels(); i++) {
            channels_.getChannelSetting(i).exposure_ = ( event.getNewExposureTime());
        }
        fireTableDataChanged();
    }



 }
