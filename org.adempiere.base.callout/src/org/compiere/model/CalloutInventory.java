/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2006 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
package org.compiere.model;

import java.math.BigDecimal;
import java.util.Properties;
import java.util.logging.Level;

import org.compiere.util.Env;

/**
 *	Physical Inventory Callouts
 *	
 *  @author Jorg Janke
 *  @version $Id: CalloutInventory.java,v 1.2 2006/07/30 00:51:03 jjanke Exp $
 */
public class CalloutInventory extends CalloutEngine
{
	/**
	 *  Product/Locator/ASI modified.
	 * 		Set Attribute Set Instance
	 *
	 *  @param ctx      Context
	 *  @param WindowNo current Window No
	 *  @param mTab     Model Tab
	 *  @param mField   Model Field
	 *  @param value    The new value
	 *  @return Error message or ""
	 */
	public String product (Properties ctx, int WindowNo, GridTab mTab, GridField mField, Object value)
	{
		if (isCalloutActive())
			return "";

		// set docSubTypeInv
		int doctypeid = Env.getContextAsInt(ctx, WindowNo, "C_DocType_ID");
		String docSubTypeInv = null;
		if (doctypeid > 0) {
			MDocType dt = MDocType.get(ctx, doctypeid);
			docSubTypeInv = dt.getDocSubTypeInv();
		}

		if ("M_Product_ID".equals(mField.getColumnName())) {
			// product changed - remove old ASI
			mTab.setValue("M_AttributeSetInstance_ID", 0);
		}

		//	Get Book Value
		int M_Product_ID = 0;
		Integer Product = (Integer)mTab.getValue("M_Product_ID");
		if (Product != null)
			M_Product_ID = Product.intValue();
		if (M_Product_ID == 0)
			return "";
		int M_Locator_ID = 0;
		Integer Locator = (Integer)mTab.getValue("M_Locator_ID");
		if (Locator != null)
			M_Locator_ID = Locator.intValue();
		if (M_Locator_ID == 0)
			return "";
		
		//	Set Attribute
		int M_AttributeSetInstance_ID = 0; 
		Integer ASI = (Integer)mTab.getValue("M_AttributeSetInstance_ID");
		if (ASI != null)
			M_AttributeSetInstance_ID = ASI.intValue();
		//	Product Selection
		if (MInventoryLine.COLUMNNAME_M_Product_ID.equals(mField.getColumnName()))
		{
			if (Env.getContextAsInt(ctx, WindowNo, Env.TAB_INFO, "M_Product_ID") == M_Product_ID)
			{
				M_AttributeSetInstance_ID = Env.getContextAsInt(ctx, WindowNo, Env.TAB_INFO, "M_AttributeSetInstance_ID");
			}
			else
			{
				M_AttributeSetInstance_ID = 0;
			}
			if (M_AttributeSetInstance_ID != 0)
				mTab.setValue(MInventoryLine.COLUMNNAME_M_AttributeSetInstance_ID, M_AttributeSetInstance_ID);
			else
				mTab.setValue(MInventoryLine.COLUMNNAME_M_AttributeSetInstance_ID, 0);
		}
			
		// Set QtyBook from first storage location
		// kviiksaar: Call's now the extracted function
		BigDecimal bd = null;
		if (MDocType.DOCSUBTYPEINV_PhysicalInventory.equals(docSubTypeInv)) {
			try {
				String trxName = null;
				if (   mTab != null
					&& mTab.getTableModel() != null) {
					GridTable gt = mTab.getTableModel();
					if (gt.isImporting()) {
						trxName = gt.get_TrxName();
					}
				}
				if (mTab.getValue("M_Inventory_ID") == null)
					return null;
				MInventory inventory = new MInventory(ctx, (Integer) mTab.getValue("M_Inventory_ID"), trxName);
				bd = MStorageOnHand.getQtyOnHandForLocatorWithASIMovementDate(M_Product_ID, M_Locator_ID, 
						M_AttributeSetInstance_ID, inventory.getMovementDate(), trxName);
				mTab.setValue("QtyBook", bd);
			} catch (Exception e) {
				return e.getLocalizedMessage();
			}
		}
		
		//
		if (log.isLoggable(Level.INFO)) log.info("M_Product_ID=" + M_Product_ID 
			+ ", M_Locator_ID=" + M_Locator_ID
			+ ", M_AttributeSetInstance_ID=" + M_AttributeSetInstance_ID
			+ " - QtyBook=" + bd);
		return "";
	}   //  product
	
}	//	CalloutInventory
