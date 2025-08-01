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

package org.adempiere.process;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.logging.Level;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.I_C_InvoiceLine;
import org.compiere.model.MInOut;
import org.compiere.model.MInOutLine;
import org.compiere.model.MInvoiceLine;
import org.compiere.model.MProcessPara;
import org.compiere.model.MRMA;
import org.compiere.model.MRMALine;
import org.compiere.model.Query;
import org.compiere.process.DocAction;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Msg;

/**
 * Generate shipment for Vendor RMA.
 * Based on {@link org.compiere.process.InOutGenerate}.
 * @author Ashley Ramdass
 * @author Teo Sarca
 * 			<li>BF [ 2818523 ] Invoice and Shipment are not matched in case of RMA
 * 				https://sourceforge.net/p/adempiere/bugs/1972/
 */
@org.adempiere.base.annotation.Process
public class InOutGenerateRMA extends SvrProcess
{
    /** Manual Selection        */
    private boolean     p_Selection = false;
    /** Warehouse               */
    @SuppressWarnings("unused")
    private int         p_M_Warehouse_ID = 0;
    /** DocAction               */
    private String      p_docAction = DocAction.ACTION_None;
    /** Number of Shipments     */
    private int         m_created = 0;
    /** Movement Date           */
    private Timestamp   p_movementDate = null;

    protected void prepare()
    {
        ProcessInfoParameter[] para = getParameter();
        for (int i = 0; i < para.length; i++)
        {
            String name = para[i].getParameterName();
            if (para[i].getParameter() == null)
                ;
            else if (name.equals("M_Warehouse_ID"))
                p_M_Warehouse_ID = para[i].getParameterAsInt();
            else if (name.equals("Selection"))
                p_Selection = "Y".equals(para[i].getParameter());
            else if (name.equals("DocAction"))
                p_docAction = (String)para[i].getParameter();
            else if (name.equals("MovementDate"))
            	p_movementDate = para[i].getParameterAsTimestamp();
            else
				MProcessPara.validateUnknownParameter(getProcessInfo().getAD_Process_ID(), para[i]);
        }
        if (p_movementDate == null) {
	        p_movementDate = Env.getContextAsDate(getCtx(), Env.DATE);
	        if (p_movementDate == null)
	        {
	            p_movementDate = new Timestamp(System.currentTimeMillis());
	        }
        }
        if (getProcessInfo().getAD_InfoWindow_ID() > 0) p_Selection=true;
    }
    
    protected String doIt() throws Exception
    {
        if (!p_Selection)
        {
            throw new IllegalStateException("Shipments can only be generated from selection");
        }
        
        String sql = "SELECT rma.M_RMA_ID FROM M_RMA rma, T_Selection "
            + "WHERE rma.DocStatus='CO' AND rma.IsSOTrx='N' AND rma.AD_Client_ID=? "
            + "AND rma.M_RMA_ID = T_Selection.T_Selection_ID " 
            + "AND T_Selection.AD_PInstance_ID=? ";
        
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try
        {
            pstmt = DB.prepareStatement(sql, get_TrxName());
            pstmt.setInt(1, Env.getAD_Client_ID(getCtx()));
            pstmt.setInt(2, getAD_PInstance_ID());
            rs = pstmt.executeQuery();
            
            while (rs.next())
            {
                generateShipment(rs.getInt(1));
            }
        }
        catch (Exception ex)
        {
			throw new AdempiereException(ex);
        }
        finally
        {
            DB.close(rs,pstmt);
            rs = null;pstmt = null;
        }
        
        StringBuilder msgreturn = new StringBuilder("@Created@ = ").append(m_created);
        return msgreturn.toString();
    }
    
    private int getShipmentDocTypeId(int M_RMA_ID)
    {
        String docTypeSQl = "SELECT dt.C_DocTypeShipment_ID FROM C_DocType dt "
            + "INNER JOIN M_RMA rma ON dt.C_DocType_ID=rma.C_DocType_ID "
            + "WHERE rma.M_RMA_ID=?";
        
        int docTypeId = DB.getSQLValue(get_TrxName(), docTypeSQl, M_RMA_ID);
        
        return docTypeId;
    }
    
    private MInOut createShipment(MRMA rma)
    {
        int docTypeId = getShipmentDocTypeId(rma.get_ID());
        
        if (docTypeId == -1)
        {
            throw new IllegalStateException("Could not get shipment document type for Vendor RMA");
        }
        
        MInOut originalReceipt = rma.getShipment();
        
        MInOut shipment = new MInOut(getCtx(), 0, get_TrxName());
        shipment.setM_RMA_ID(rma.get_ID());
        shipment.setAD_Org_ID(rma.getAD_Org_ID());
        shipment.setAD_OrgTrx_ID(originalReceipt.getAD_OrgTrx_ID());
        shipment.setDescription(rma.getDescription());
        shipment.setC_BPartner_ID(rma.getC_BPartner_ID());
        shipment.setC_BPartner_Location_ID(originalReceipt.getC_BPartner_Location_ID());
        shipment.setIsSOTrx(rma.isSOTrx());
        shipment.setC_DocType_ID(docTypeId);       
        shipment.setM_Warehouse_ID(originalReceipt.getM_Warehouse_ID());
        shipment.setMovementType(MInOut.MOVEMENTTYPE_VendorReturns);
        shipment.setC_Project_ID(originalReceipt.getC_Project_ID());
        shipment.setC_Campaign_ID(originalReceipt.getC_Campaign_ID());
        shipment.setC_Activity_ID(originalReceipt.getC_Activity_ID());
        shipment.setUser1_ID(originalReceipt.getUser1_ID());
        shipment.setUser2_ID(originalReceipt.getUser2_ID());
        shipment.setC_CostCenter_ID(originalReceipt.getC_CostCenter_ID());
        shipment.setC_Department_ID(originalReceipt.getC_Department_ID());
        shipment.setMovementDate(p_movementDate);
        shipment.setDateAcct(p_movementDate);
        
        if (!shipment.save())
        {
            throw new IllegalStateException("Could not create Shipment");
        }
        
        return shipment;
    }
    
    private MInOutLine[] createShipmentLines(MRMA rma, MInOut shipment)
    {
        ArrayList<MInOutLine> shipLineList = new ArrayList<MInOutLine>();
        
        MRMALine rmaLines[] = rma.getLines(true);
        for (MRMALine rmaLine : rmaLines)
        {
        	if (rmaLine.getM_InOutLine_ID() != 0 || rmaLine.getC_Charge_ID() != 0 || rmaLine.getM_Product_ID() != 0)
            {
                MInOutLine shipLine = new MInOutLine(shipment);
                shipLine.setM_RMALine_ID(rmaLine.get_ID());
                shipLine.setLine(rmaLine.getLine());
                shipLine.setDescription(rmaLine.getDescription());
                
                if (rmaLine.getC_Charge_ID() != 0)
                {
                	shipLine.setC_Charge_ID(rmaLine.getC_Charge_ID());
                	shipLine.set_ValueNoCheck(MInOutLine.COLUMNNAME_M_Product_ID, null);
                	shipLine.set_ValueNoCheck(MInOutLine.COLUMNNAME_M_AttributeSetInstance_ID, null);
                	shipLine.set_ValueNoCheck(MInOutLine.COLUMNNAME_M_Locator_ID, null);
                }
                else
                {
                	shipLine.setM_Product_ID(rmaLine.getM_Product_ID());
                    shipLine.setM_AttributeSetInstance_ID(rmaLine.getM_AttributeSetInstance_ID());
                    shipLine.setM_Locator_ID(rmaLine.getM_Locator_ID());
                }
                
                shipLine.setC_UOM_ID(rmaLine.getC_UOM_ID());
                shipLine.setQty(rmaLine.getQty());
                shipLine.setC_Project_ID(rmaLine.getC_Project_ID());
                shipLine.setC_Campaign_ID(rmaLine.getC_Campaign_ID());
                shipLine.setC_Activity_ID(rmaLine.getC_Activity_ID());
                shipLine.setC_ProjectPhase_ID(rmaLine.getC_ProjectPhase_ID());
                shipLine.setC_ProjectTask_ID(rmaLine.getC_ProjectTask_ID());
                shipLine.setUser1_ID(rmaLine.getUser1_ID());
                shipLine.setUser2_ID(rmaLine.getUser2_ID());
				shipLine.setC_CostCenter_ID(rmaLine.getC_CostCenter_ID());
				shipLine.setC_Department_ID(rmaLine.getC_Department_ID());
                shipLine.saveEx();
                shipLineList.add(shipLine);
                //
                // Link to corresponding Invoice Line (if any) - teo_sarca [ 2818523 ]
                // The MMatchInv records will be automatically generated on MInOut.completeIt()
            	MInvoiceLine invoiceLine = new Query(shipment.getCtx(), I_C_InvoiceLine.Table_Name,
            			I_C_InvoiceLine.COLUMNNAME_M_RMALine_ID+"=?",
            			shipment.get_TrxName())
            	.setParameters(rmaLine.getM_RMALine_ID())
            	.firstOnly();
            	if (invoiceLine != null)
            	{
            		invoiceLine.setM_InOutLine_ID(shipLine.getM_InOutLine_ID());
            		invoiceLine.saveEx();
            	}

            }
        }
        
        MInOutLine shipLines[] = new MInOutLine[shipLineList.size()];
        shipLineList.toArray(shipLines);
        
        return shipLines;
    }
    
    private void generateShipment(int M_RMA_ID)
    {
        MRMA rma = new MRMA(getCtx(), M_RMA_ID, get_TrxName());
        statusUpdate(Msg.getMsg(getCtx(), "Processing") + " " + rma.getDocumentInfo());
        
        MInOut shipment = createShipment(rma);
        MInOutLine shipmentLines[] = createShipmentLines(rma, shipment);
        
        if (shipmentLines.length == 0)
        {
            StringBuilder msglog = new StringBuilder("No shipment lines created: M_RMA_ID=")
                    .append(M_RMA_ID).append(", M_InOut_ID=").append(shipment.get_ID());
        	log.log(Level.WARNING, msglog.toString());
        }
        
        StringBuilder processMsg = new StringBuilder().append(shipment.getDocumentNo());
        
		if (!DocAction.ACTION_None.equals(p_docAction)) {
	        if (!shipment.processIt(p_docAction))
	        {
	            processMsg.append(" (NOT Processed)");
	            StringBuilder msglog = new StringBuilder("Shipment Processing failed: ").append(shipment).append(" - ").append(shipment.getProcessMsg());
	            log.warning(msglog.toString());
	            throw new IllegalStateException("Shipment Processing failed: " + shipment + " - " + shipment.getProcessMsg());
	        }
		}
        shipment.saveEx();
        
        // Add processing information to process log
        addBufferLog(shipment.getM_InOut_ID(), shipment.getMovementDate(), null, processMsg.toString(),shipment.get_Table_ID(),shipment.getM_InOut_ID());
        m_created++;
    }
    
}
