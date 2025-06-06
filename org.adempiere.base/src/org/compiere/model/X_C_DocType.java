/******************************************************************************
 * Product: iDempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2012 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software, you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY, without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program, if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
/** Generated Model - DO NOT CHANGE */
package org.compiere.model;

import java.sql.ResultSet;
import java.util.Properties;
import org.compiere.util.KeyNamePair;

/** Generated Model for C_DocType
 *  @author iDempiere (generated)
 *  @version Release 13 - $Id$ */
@org.adempiere.base.Model(table="C_DocType")
public class X_C_DocType extends PO implements I_C_DocType, I_Persistent
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20250520L;

    /** Standard Constructor */
    public X_C_DocType (Properties ctx, int C_DocType_ID, String trxName)
    {
      super (ctx, C_DocType_ID, trxName);
      /** if (C_DocType_ID == 0)
        {
			setC_DocType_ID (0);
			setDocBaseType (null);
			setDocumentCopies (0);
// 1
			setGL_Category_ID (0);
			setHasCharges (false);
			setIsAlwaysPosted (false);
// N
			setIsAutoGenerateInout (false);
// N
			setIsAutoGenerateInvoice (false);
// N
			setIsCanBeReactivated (false);
// N
			setIsCreateCounter (true);
// Y
			setIsDefault (false);
			setIsDefaultCounterDoc (false);
			setIsDocNoControlled (true);
// Y
			setIsInTransit (false);
			setIsIndexed (false);
			setIsPickQAConfirm (false);
			setIsPrepareSplitDocument (true);
// Y
			setIsSOTrx (false);
			setIsShipConfirm (false);
			setIsSplitWhenDifference (false);
// N
			setName (null);
			setPrintName (null);
        } */
    }

    /** Standard Constructor */
    public X_C_DocType (Properties ctx, int C_DocType_ID, String trxName, String ... virtualColumns)
    {
      super (ctx, C_DocType_ID, trxName, virtualColumns);
      /** if (C_DocType_ID == 0)
        {
			setC_DocType_ID (0);
			setDocBaseType (null);
			setDocumentCopies (0);
// 1
			setGL_Category_ID (0);
			setHasCharges (false);
			setIsAlwaysPosted (false);
// N
			setIsAutoGenerateInout (false);
// N
			setIsAutoGenerateInvoice (false);
// N
			setIsCanBeReactivated (false);
// N
			setIsCreateCounter (true);
// Y
			setIsDefault (false);
			setIsDefaultCounterDoc (false);
			setIsDocNoControlled (true);
// Y
			setIsInTransit (false);
			setIsIndexed (false);
			setIsPickQAConfirm (false);
			setIsPrepareSplitDocument (true);
// Y
			setIsSOTrx (false);
			setIsShipConfirm (false);
			setIsSplitWhenDifference (false);
// N
			setName (null);
			setPrintName (null);
        } */
    }

    /** Standard Constructor */
    public X_C_DocType (Properties ctx, String C_DocType_UU, String trxName)
    {
      super (ctx, C_DocType_UU, trxName);
      /** if (C_DocType_UU == null)
        {
			setC_DocType_ID (0);
			setDocBaseType (null);
			setDocumentCopies (0);
// 1
			setGL_Category_ID (0);
			setHasCharges (false);
			setIsAlwaysPosted (false);
// N
			setIsAutoGenerateInout (false);
// N
			setIsAutoGenerateInvoice (false);
// N
			setIsCanBeReactivated (false);
// N
			setIsCreateCounter (true);
// Y
			setIsDefault (false);
			setIsDefaultCounterDoc (false);
			setIsDocNoControlled (true);
// Y
			setIsInTransit (false);
			setIsIndexed (false);
			setIsPickQAConfirm (false);
			setIsPrepareSplitDocument (true);
// Y
			setIsSOTrx (false);
			setIsShipConfirm (false);
			setIsSplitWhenDifference (false);
// N
			setName (null);
			setPrintName (null);
        } */
    }

    /** Standard Constructor */
    public X_C_DocType (Properties ctx, String C_DocType_UU, String trxName, String ... virtualColumns)
    {
      super (ctx, C_DocType_UU, trxName, virtualColumns);
      /** if (C_DocType_UU == null)
        {
			setC_DocType_ID (0);
			setDocBaseType (null);
			setDocumentCopies (0);
// 1
			setGL_Category_ID (0);
			setHasCharges (false);
			setIsAlwaysPosted (false);
// N
			setIsAutoGenerateInout (false);
// N
			setIsAutoGenerateInvoice (false);
// N
			setIsCanBeReactivated (false);
// N
			setIsCreateCounter (true);
// Y
			setIsDefault (false);
			setIsDefaultCounterDoc (false);
			setIsDocNoControlled (true);
// Y
			setIsInTransit (false);
			setIsIndexed (false);
			setIsPickQAConfirm (false);
			setIsPrepareSplitDocument (true);
// Y
			setIsSOTrx (false);
			setIsShipConfirm (false);
			setIsSplitWhenDifference (false);
// N
			setName (null);
			setPrintName (null);
        } */
    }

    /** Load Constructor */
    public X_C_DocType (Properties ctx, ResultSet rs, String trxName)
    {
      super (ctx, rs, trxName);
    }

    /** AccessLevel
      * @return 6 - System - Client
      */
    protected int get_AccessLevel()
    {
      return accessLevel.intValue();
    }

    /** Load Meta Data */
    protected POInfo initPO (Properties ctx)
    {
      POInfo poi = POInfo.getPOInfo (ctx, Table_ID, get_TrxName());
      return poi;
    }

    public String toString()
    {
      StringBuilder sb = new StringBuilder ("X_C_DocType[")
        .append(get_ID()).append(",Name=").append(getName()).append("]");
      return sb.toString();
    }

	public org.compiere.model.I_AD_PrintFormat getAD_PrintFormat() throws RuntimeException
	{
		return (org.compiere.model.I_AD_PrintFormat)MTable.get(getCtx(), org.compiere.model.I_AD_PrintFormat.Table_ID)
			.getPO(getAD_PrintFormat_ID(), get_TrxName());
	}

	/** Set Print Format.
		@param AD_PrintFormat_ID Data Print Format
	*/
	public void setAD_PrintFormat_ID (int AD_PrintFormat_ID)
	{
		if (AD_PrintFormat_ID < 1)
			set_Value (COLUMNNAME_AD_PrintFormat_ID, null);
		else
			set_Value (COLUMNNAME_AD_PrintFormat_ID, Integer.valueOf(AD_PrintFormat_ID));
	}

	/** Get Print Format.
		@return Data Print Format
	  */
	public int getAD_PrintFormat_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_AD_PrintFormat_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public org.compiere.model.I_C_DocType getC_DocTypeDifference() throws RuntimeException
	{
		return (org.compiere.model.I_C_DocType)MTable.get(getCtx(), org.compiere.model.I_C_DocType.Table_ID)
			.getPO(getC_DocTypeDifference_ID(), get_TrxName());
	}

	/** Set Difference Document.
		@param C_DocTypeDifference_ID Document type for generating in dispute Shipments
	*/
	public void setC_DocTypeDifference_ID (int C_DocTypeDifference_ID)
	{
		if (C_DocTypeDifference_ID < 1)
			set_Value (COLUMNNAME_C_DocTypeDifference_ID, null);
		else
			set_Value (COLUMNNAME_C_DocTypeDifference_ID, Integer.valueOf(C_DocTypeDifference_ID));
	}

	/** Get Difference Document.
		@return Document type for generating in dispute Shipments
	  */
	public int getC_DocTypeDifference_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_DocTypeDifference_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public org.compiere.model.I_C_DocType getC_DocTypeInvoice() throws RuntimeException
	{
		return (org.compiere.model.I_C_DocType)MTable.get(getCtx(), org.compiere.model.I_C_DocType.Table_ID)
			.getPO(getC_DocTypeInvoice_ID(), get_TrxName());
	}

	/** Set Document Type for Invoice.
		@param C_DocTypeInvoice_ID Document type used for invoices generated from this sales document
	*/
	public void setC_DocTypeInvoice_ID (int C_DocTypeInvoice_ID)
	{
		if (C_DocTypeInvoice_ID < 1)
			set_Value (COLUMNNAME_C_DocTypeInvoice_ID, null);
		else
			set_Value (COLUMNNAME_C_DocTypeInvoice_ID, Integer.valueOf(C_DocTypeInvoice_ID));
	}

	/** Get Document Type for Invoice.
		@return Document type used for invoices generated from this sales document
	  */
	public int getC_DocTypeInvoice_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_DocTypeInvoice_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public org.compiere.model.I_C_DocType getC_DocTypeProforma() throws RuntimeException
	{
		return (org.compiere.model.I_C_DocType)MTable.get(getCtx(), org.compiere.model.I_C_DocType.Table_ID)
			.getPO(getC_DocTypeProforma_ID(), get_TrxName());
	}

	/** Set Document Type for ProForma.
		@param C_DocTypeProforma_ID Document type used for pro forma invoices generated from this sales document
	*/
	public void setC_DocTypeProforma_ID (int C_DocTypeProforma_ID)
	{
		if (C_DocTypeProforma_ID < 1)
			set_Value (COLUMNNAME_C_DocTypeProforma_ID, null);
		else
			set_Value (COLUMNNAME_C_DocTypeProforma_ID, Integer.valueOf(C_DocTypeProforma_ID));
	}

	/** Get Document Type for ProForma.
		@return Document type used for pro forma invoices generated from this sales document
	  */
	public int getC_DocTypeProforma_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_DocTypeProforma_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public org.compiere.model.I_C_DocType getC_DocTypeShipment() throws RuntimeException
	{
		return (org.compiere.model.I_C_DocType)MTable.get(getCtx(), org.compiere.model.I_C_DocType.Table_ID)
			.getPO(getC_DocTypeShipment_ID(), get_TrxName());
	}

	/** Set Document Type for Shipment.
		@param C_DocTypeShipment_ID Document type used for shipments generated from this sales document
	*/
	public void setC_DocTypeShipment_ID (int C_DocTypeShipment_ID)
	{
		if (C_DocTypeShipment_ID < 1)
			set_Value (COLUMNNAME_C_DocTypeShipment_ID, null);
		else
			set_Value (COLUMNNAME_C_DocTypeShipment_ID, Integer.valueOf(C_DocTypeShipment_ID));
	}

	/** Get Document Type for Shipment.
		@return Document type used for shipments generated from this sales document
	  */
	public int getC_DocTypeShipment_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_DocTypeShipment_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Document Type.
		@param C_DocType_ID Document type or rules
	*/
	public void setC_DocType_ID (int C_DocType_ID)
	{
		if (C_DocType_ID < 0)
			set_ValueNoCheck (COLUMNNAME_C_DocType_ID, null);
		else
			set_ValueNoCheck (COLUMNNAME_C_DocType_ID, Integer.valueOf(C_DocType_ID));
	}

	/** Get Document Type.
		@return Document type or rules
	  */
	public int getC_DocType_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_DocType_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set C_DocType_UU.
		@param C_DocType_UU C_DocType_UU
	*/
	public void setC_DocType_UU (String C_DocType_UU)
	{
		set_Value (COLUMNNAME_C_DocType_UU, C_DocType_UU);
	}

	/** Get C_DocType_UU.
		@return C_DocType_UU	  */
	public String getC_DocType_UU()
	{
		return (String)get_Value(COLUMNNAME_C_DocType_UU);
	}

	public org.compiere.model.I_AD_Sequence getDefiniteSequence() throws RuntimeException
	{
		return (org.compiere.model.I_AD_Sequence)MTable.get(getCtx(), org.compiere.model.I_AD_Sequence.Table_ID)
			.getPO(getDefiniteSequence_ID(), get_TrxName());
	}

	/** Set Definite Sequence.
		@param DefiniteSequence_ID Definite Sequence
	*/
	public void setDefiniteSequence_ID (int DefiniteSequence_ID)
	{
		if (DefiniteSequence_ID < 1)
			set_Value (COLUMNNAME_DefiniteSequence_ID, null);
		else
			set_Value (COLUMNNAME_DefiniteSequence_ID, Integer.valueOf(DefiniteSequence_ID));
	}

	/** Get Definite Sequence.
		@return Definite Sequence	  */
	public int getDefiniteSequence_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_DefiniteSequence_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Description.
		@param Description Optional short description of the record
	*/
	public void setDescription (String Description)
	{
		set_Value (COLUMNNAME_Description, Description);
	}

	/** Get Description.
		@return Optional short description of the record
	  */
	public String getDescription()
	{
		return (String)get_Value(COLUMNNAME_Description);
	}

	/** DocBaseType AD_Reference_ID=183 */
	public static final int DOCBASETYPE_AD_Reference_ID=183;
	/** AP Credit Memo = APC */
	public static final String DOCBASETYPE_APCreditMemo = "APC";
	/** AP Invoice = API */
	public static final String DOCBASETYPE_APInvoice = "API";
	/** AP Payment = APP */
	public static final String DOCBASETYPE_APPayment = "APP";
	/** AR Credit Memo = ARC */
	public static final String DOCBASETYPE_ARCreditMemo = "ARC";
	/** AR Pro Forma Invoice = ARF */
	public static final String DOCBASETYPE_ARProFormaInvoice = "ARF";
	/** AR Invoice = ARI */
	public static final String DOCBASETYPE_ARInvoice = "ARI";
	/** AR Receipt = ARR */
	public static final String DOCBASETYPE_ARReceipt = "ARR";
	/** Payment Allocation = CMA */
	public static final String DOCBASETYPE_PaymentAllocation = "CMA";
	/** Bank Statement = CMB */
	public static final String DOCBASETYPE_BankStatement = "CMB";
	/** Cash Journal = CMC */
	public static final String DOCBASETYPE_CashJournal = "CMC";
	/** Distribution Order = DOO */
	public static final String DOCBASETYPE_DistributionOrder = "DOO";
	/** Fixed Assets Addition = FAA */
	public static final String DOCBASETYPE_FixedAssetsAddition = "FAA";
	/** Fixed Assets Disposal = FAD */
	public static final String DOCBASETYPE_FixedAssetsDisposal = "FAD";
	/** Fixed Assets Depreciation = FDP */
	public static final String DOCBASETYPE_FixedAssetsDepreciation = "FDP";
	/** GL Document = GLD */
	public static final String DOCBASETYPE_GLDocument = "GLD";
	/** GL Journal = GLJ */
	public static final String DOCBASETYPE_GLJournal = "GLJ";
	/** Payroll = HRP */
	public static final String DOCBASETYPE_Payroll = "HRP";
	/** Manufacturing Cost Collector = MCC */
	public static final String DOCBASETYPE_ManufacturingCostCollector = "MCC";
	/** Material Physical Inventory = MMI */
	public static final String DOCBASETYPE_MaterialPhysicalInventory = "MMI";
	/** Material Movement = MMM */
	public static final String DOCBASETYPE_MaterialMovement = "MMM";
	/** Material Production = MMP */
	public static final String DOCBASETYPE_MaterialProduction = "MMP";
	/** Material Receipt = MMR */
	public static final String DOCBASETYPE_MaterialReceipt = "MMR";
	/** Material Delivery = MMS */
	public static final String DOCBASETYPE_MaterialDelivery = "MMS";
	/** Maintenance Order = MOF */
	public static final String DOCBASETYPE_MaintenanceOrder = "MOF";
	/** Manufacturing Order = MOP */
	public static final String DOCBASETYPE_ManufacturingOrder = "MOP";
	/** Quality Order = MQO */
	public static final String DOCBASETYPE_QualityOrder = "MQO";
	/** Match Invoice = MXI */
	public static final String DOCBASETYPE_MatchInvoice = "MXI";
	/** Match PO = MXP */
	public static final String DOCBASETYPE_MatchPO = "MXP";
	/** Project Issue = PJI */
	public static final String DOCBASETYPE_ProjectIssue = "PJI";
	/** Purchase Order = POO */
	public static final String DOCBASETYPE_PurchaseOrder = "POO";
	/** Purchase Requisition = POR */
	public static final String DOCBASETYPE_PurchaseRequisition = "POR";
	/** Sales Order = SOO */
	public static final String DOCBASETYPE_SalesOrder = "SOO";
	/** Set Document Base Type.
		@param DocBaseType Logical type of document
	*/
	public void setDocBaseType (String DocBaseType)
	{

		set_Value (COLUMNNAME_DocBaseType, DocBaseType);
	}

	/** Get Document Base Type.
		@return Logical type of document
	  */
	public String getDocBaseType()
	{
		return (String)get_Value(COLUMNNAME_DocBaseType);
	}

	public org.compiere.model.I_AD_Sequence getDocNoSequence() throws RuntimeException
	{
		return (org.compiere.model.I_AD_Sequence)MTable.get(getCtx(), org.compiere.model.I_AD_Sequence.Table_ID)
			.getPO(getDocNoSequence_ID(), get_TrxName());
	}

	/** Set Document Sequence.
		@param DocNoSequence_ID Document sequence determines the numbering of documents
	*/
	public void setDocNoSequence_ID (int DocNoSequence_ID)
	{
		if (DocNoSequence_ID < 1)
			set_Value (COLUMNNAME_DocNoSequence_ID, null);
		else
			set_Value (COLUMNNAME_DocNoSequence_ID, Integer.valueOf(DocNoSequence_ID));
	}

	/** Get Document Sequence.
		@return Document sequence determines the numbering of documents
	  */
	public int getDocNoSequence_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_DocNoSequence_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** DocSubTypeInv AD_Reference_ID=200068 */
	public static final int DOCSUBTYPEINV_AD_Reference_ID=200068;
	/** Cost Adjustment = CA */
	public static final String DOCSUBTYPEINV_CostAdjustment = "CA";
	/** Internal Use Inventory = IU */
	public static final String DOCSUBTYPEINV_InternalUseInventory = "IU";
	/** Physical Inventory = PI */
	public static final String DOCSUBTYPEINV_PhysicalInventory = "PI";
	/** Set Inv Sub Type.
		@param DocSubTypeInv Inventory Sub Type
	*/
	public void setDocSubTypeInv (String DocSubTypeInv)
	{

		set_Value (COLUMNNAME_DocSubTypeInv, DocSubTypeInv);
	}

	/** Get Inv Sub Type.
		@return Inventory Sub Type
	  */
	public String getDocSubTypeInv()
	{
		return (String)get_Value(COLUMNNAME_DocSubTypeInv);
	}

	/** DocSubTypeSO AD_Reference_ID=148 */
	public static final int DOCSUBTYPESO_AD_Reference_ID=148;
	/** Quotation = OB */
	public static final String DOCSUBTYPESO_Quotation = "OB";
	/** Proposal = ON */
	public static final String DOCSUBTYPESO_Proposal = "ON";
	/** Prepay Order = PR */
	public static final String DOCSUBTYPESO_PrepayOrder = "PR";
	/** Return Material = RM */
	public static final String DOCSUBTYPESO_ReturnMaterial = "RM";
	/** Standard Order = SO */
	public static final String DOCSUBTYPESO_StandardOrder = "SO";
	/** On Credit Order = WI */
	public static final String DOCSUBTYPESO_OnCreditOrder = "WI";
	/** Warehouse Order = WP */
	public static final String DOCSUBTYPESO_WarehouseOrder = "WP";
	/** POS Order = WR */
	public static final String DOCSUBTYPESO_POSOrder = "WR";
	/** Set SO Sub Type.
		@param DocSubTypeSO Sales Order Sub Type
	*/
	public void setDocSubTypeSO (String DocSubTypeSO)
	{

		set_Value (COLUMNNAME_DocSubTypeSO, DocSubTypeSO);
	}

	/** Get SO Sub Type.
		@return Sales Order Sub Type
	  */
	public String getDocSubTypeSO()
	{
		return (String)get_Value(COLUMNNAME_DocSubTypeSO);
	}

	/** Set Document Copies.
		@param DocumentCopies Number of copies to be printed
	*/
	public void setDocumentCopies (int DocumentCopies)
	{
		set_Value (COLUMNNAME_DocumentCopies, Integer.valueOf(DocumentCopies));
	}

	/** Get Document Copies.
		@return Number of copies to be printed
	  */
	public int getDocumentCopies()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_DocumentCopies);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Document Note.
		@param DocumentNote Additional information for a Document
	*/
	public void setDocumentNote (String DocumentNote)
	{
		set_Value (COLUMNNAME_DocumentNote, DocumentNote);
	}

	/** Get Document Note.
		@return Additional information for a Document
	  */
	public String getDocumentNote()
	{
		return (String)get_Value(COLUMNNAME_DocumentNote);
	}

	public org.compiere.model.I_GL_Category getGL_Category() throws RuntimeException
	{
		return (org.compiere.model.I_GL_Category)MTable.get(getCtx(), org.compiere.model.I_GL_Category.Table_ID)
			.getPO(getGL_Category_ID(), get_TrxName());
	}

	/** Set GL Category.
		@param GL_Category_ID General Ledger Category
	*/
	public void setGL_Category_ID (int GL_Category_ID)
	{
		if (GL_Category_ID < 1)
			set_Value (COLUMNNAME_GL_Category_ID, null);
		else
			set_Value (COLUMNNAME_GL_Category_ID, Integer.valueOf(GL_Category_ID));
	}

	/** Get GL Category.
		@return General Ledger Category
	  */
	public int getGL_Category_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_GL_Category_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Charges.
		@param HasCharges Charges can be added to the document
	*/
	public void setHasCharges (boolean HasCharges)
	{
		set_Value (COLUMNNAME_HasCharges, Boolean.valueOf(HasCharges));
	}

	/** Get Charges.
		@return Charges can be added to the document
	  */
	public boolean isHasCharges()
	{
		Object oo = get_Value(COLUMNNAME_HasCharges);
		if (oo != null)
		{
			 if (oo instanceof Boolean)
				 return ((Boolean)oo).booleanValue();
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Pro forma Invoice.
		@param HasProforma Indicates if Pro Forma Invoices can be generated from this document
	*/
	public void setHasProforma (boolean HasProforma)
	{
		set_Value (COLUMNNAME_HasProforma, Boolean.valueOf(HasProforma));
	}

	/** Get Pro forma Invoice.
		@return Indicates if Pro Forma Invoices can be generated from this document
	  */
	public boolean isHasProforma()
	{
		Object oo = get_Value(COLUMNNAME_HasProforma);
		if (oo != null)
		{
			 if (oo instanceof Boolean)
				 return ((Boolean)oo).booleanValue();
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Always Posted.
		@param IsAlwaysPosted Posts the document if &quot;AlwaysPosted&quot; is checked, even without a valid accounting schema.
	*/
	public void setIsAlwaysPosted (boolean IsAlwaysPosted)
	{
		set_Value (COLUMNNAME_IsAlwaysPosted, Boolean.valueOf(IsAlwaysPosted));
	}

	/** Get Always Posted.
		@return Posts the document if &quot;AlwaysPosted&quot; is checked, even without a valid accounting schema.
	  */
	public boolean isAlwaysPosted()
	{
		Object oo = get_Value(COLUMNNAME_IsAlwaysPosted);
		if (oo != null)
		{
			 if (oo instanceof Boolean)
				 return ((Boolean)oo).booleanValue();
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Generate Shipment.
		@param IsAutoGenerateInout Automatically Generate Shipment after (Sales/Purchase) Order completed
	*/
	public void setIsAutoGenerateInout (boolean IsAutoGenerateInout)
	{
		set_Value (COLUMNNAME_IsAutoGenerateInout, Boolean.valueOf(IsAutoGenerateInout));
	}

	/** Get Generate Shipment.
		@return Automatically Generate Shipment after (Sales/Purchase) Order completed
	  */
	public boolean isAutoGenerateInout()
	{
		Object oo = get_Value(COLUMNNAME_IsAutoGenerateInout);
		if (oo != null)
		{
			 if (oo instanceof Boolean)
				 return ((Boolean)oo).booleanValue();
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Generate Invoice.
		@param IsAutoGenerateInvoice Automatically Generate Invoice after (Sales/Purchase) Order completed
	*/
	public void setIsAutoGenerateInvoice (boolean IsAutoGenerateInvoice)
	{
		set_Value (COLUMNNAME_IsAutoGenerateInvoice, Boolean.valueOf(IsAutoGenerateInvoice));
	}

	/** Get Generate Invoice.
		@return Automatically Generate Invoice after (Sales/Purchase) Order completed
	  */
	public boolean isAutoGenerateInvoice()
	{
		Object oo = get_Value(COLUMNNAME_IsAutoGenerateInvoice);
		if (oo != null)
		{
			 if (oo instanceof Boolean)
				 return ((Boolean)oo).booleanValue();
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Can Be Reactivated.
		@param IsCanBeReactivated This document can be reactivated
	*/
	public void setIsCanBeReactivated (boolean IsCanBeReactivated)
	{
		set_Value (COLUMNNAME_IsCanBeReactivated, Boolean.valueOf(IsCanBeReactivated));
	}

	/** Get Can Be Reactivated.
		@return This document can be reactivated
	  */
	public boolean isCanBeReactivated()
	{
		Object oo = get_Value(COLUMNNAME_IsCanBeReactivated);
		if (oo != null)
		{
			 if (oo instanceof Boolean)
				 return ((Boolean)oo).booleanValue();
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Mandatory Charge or Product.
		@param IsChargeOrProductMandatory Mandatory Charge or Product
	*/
	public void setIsChargeOrProductMandatory (boolean IsChargeOrProductMandatory)
	{
		set_Value (COLUMNNAME_IsChargeOrProductMandatory, Boolean.valueOf(IsChargeOrProductMandatory));
	}

	/** Get Mandatory Charge or Product.
		@return Mandatory Charge or Product	  */
	public boolean isChargeOrProductMandatory()
	{
		Object oo = get_Value(COLUMNNAME_IsChargeOrProductMandatory);
		if (oo != null)
		{
			 if (oo instanceof Boolean)
				 return ((Boolean)oo).booleanValue();
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Create Counter Document.
		@param IsCreateCounter Create Counter Document
	*/
	public void setIsCreateCounter (boolean IsCreateCounter)
	{
		set_Value (COLUMNNAME_IsCreateCounter, Boolean.valueOf(IsCreateCounter));
	}

	/** Get Create Counter Document.
		@return Create Counter Document
	  */
	public boolean isCreateCounter()
	{
		Object oo = get_Value(COLUMNNAME_IsCreateCounter);
		if (oo != null)
		{
			 if (oo instanceof Boolean)
				 return ((Boolean)oo).booleanValue();
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Default.
		@param IsDefault Default value
	*/
	public void setIsDefault (boolean IsDefault)
	{
		set_Value (COLUMNNAME_IsDefault, Boolean.valueOf(IsDefault));
	}

	/** Get Default.
		@return Default value
	  */
	public boolean isDefault()
	{
		Object oo = get_Value(COLUMNNAME_IsDefault);
		if (oo != null)
		{
			 if (oo instanceof Boolean)
				 return ((Boolean)oo).booleanValue();
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Default Counter Document.
		@param IsDefaultCounterDoc The document type is the default counter document type
	*/
	public void setIsDefaultCounterDoc (boolean IsDefaultCounterDoc)
	{
		set_Value (COLUMNNAME_IsDefaultCounterDoc, Boolean.valueOf(IsDefaultCounterDoc));
	}

	/** Get Default Counter Document.
		@return The document type is the default counter document type
	  */
	public boolean isDefaultCounterDoc()
	{
		Object oo = get_Value(COLUMNNAME_IsDefaultCounterDoc);
		if (oo != null)
		{
			 if (oo instanceof Boolean)
				 return ((Boolean)oo).booleanValue();
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Document is Number Controlled.
		@param IsDocNoControlled The document has a document sequence
	*/
	public void setIsDocNoControlled (boolean IsDocNoControlled)
	{
		set_Value (COLUMNNAME_IsDocNoControlled, Boolean.valueOf(IsDocNoControlled));
	}

	/** Get Document is Number Controlled.
		@return The document has a document sequence
	  */
	public boolean isDocNoControlled()
	{
		Object oo = get_Value(COLUMNNAME_IsDocNoControlled);
		if (oo != null)
		{
			 if (oo instanceof Boolean)
				 return ((Boolean)oo).booleanValue();
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set In Transit.
		@param IsInTransit Movement is in transit
	*/
	public void setIsInTransit (boolean IsInTransit)
	{
		set_Value (COLUMNNAME_IsInTransit, Boolean.valueOf(IsInTransit));
	}

	/** Get In Transit.
		@return Movement is in transit
	  */
	public boolean isInTransit()
	{
		Object oo = get_Value(COLUMNNAME_IsInTransit);
		if (oo != null)
		{
			 if (oo instanceof Boolean)
				 return ((Boolean)oo).booleanValue();
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Indexed.
		@param IsIndexed Index the document for the internal search engine
	*/
	public void setIsIndexed (boolean IsIndexed)
	{
		set_Value (COLUMNNAME_IsIndexed, Boolean.valueOf(IsIndexed));
	}

	/** Get Indexed.
		@return Index the document for the internal search engine
	  */
	public boolean isIndexed()
	{
		Object oo = get_Value(COLUMNNAME_IsIndexed);
		if (oo != null)
		{
			 if (oo instanceof Boolean)
				 return ((Boolean)oo).booleanValue();
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Allow Product Without Price List.
		@param IsNoPriceListCheck Allow Product Without Price List
	*/
	public void setIsNoPriceListCheck (boolean IsNoPriceListCheck)
	{
		set_Value (COLUMNNAME_IsNoPriceListCheck, Boolean.valueOf(IsNoPriceListCheck));
	}

	/** Get Allow Product Without Price List.
		@return Allow Product Without Price List	  */
	public boolean isNoPriceListCheck()
	{
		Object oo = get_Value(COLUMNNAME_IsNoPriceListCheck);
		if (oo != null)
		{
			 if (oo instanceof Boolean)
				 return ((Boolean)oo).booleanValue();
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Overwrite Date on Complete.
		@param IsOverwriteDateOnComplete Overwrite Date on Complete
	*/
	public void setIsOverwriteDateOnComplete (boolean IsOverwriteDateOnComplete)
	{
		set_Value (COLUMNNAME_IsOverwriteDateOnComplete, Boolean.valueOf(IsOverwriteDateOnComplete));
	}

	/** Get Overwrite Date on Complete.
		@return Overwrite Date on Complete	  */
	public boolean isOverwriteDateOnComplete()
	{
		Object oo = get_Value(COLUMNNAME_IsOverwriteDateOnComplete);
		if (oo != null)
		{
			 if (oo instanceof Boolean)
				 return ((Boolean)oo).booleanValue();
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Overwrite Sequence on Complete.
		@param IsOverwriteSeqOnComplete Overwrite Sequence on Complete
	*/
	public void setIsOverwriteSeqOnComplete (boolean IsOverwriteSeqOnComplete)
	{
		set_Value (COLUMNNAME_IsOverwriteSeqOnComplete, Boolean.valueOf(IsOverwriteSeqOnComplete));
	}

	/** Get Overwrite Sequence on Complete.
		@return Overwrite Sequence on Complete	  */
	public boolean isOverwriteSeqOnComplete()
	{
		Object oo = get_Value(COLUMNNAME_IsOverwriteSeqOnComplete);
		if (oo != null)
		{
			 if (oo instanceof Boolean)
				 return ((Boolean)oo).booleanValue();
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Pick/QA Confirmation.
		@param IsPickQAConfirm Require Pick or QA Confirmation before processing
	*/
	public void setIsPickQAConfirm (boolean IsPickQAConfirm)
	{
		set_Value (COLUMNNAME_IsPickQAConfirm, Boolean.valueOf(IsPickQAConfirm));
	}

	/** Get Pick/QA Confirmation.
		@return Require Pick or QA Confirmation before processing
	  */
	public boolean isPickQAConfirm()
	{
		Object oo = get_Value(COLUMNNAME_IsPickQAConfirm);
		if (oo != null)
		{
			 if (oo instanceof Boolean)
				 return ((Boolean)oo).booleanValue();
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Prepare Split Document.
		@param IsPrepareSplitDocument Prepare generated split shipment/receipt document
	*/
	public void setIsPrepareSplitDocument (boolean IsPrepareSplitDocument)
	{
		set_Value (COLUMNNAME_IsPrepareSplitDocument, Boolean.valueOf(IsPrepareSplitDocument));
	}

	/** Get Prepare Split Document.
		@return Prepare generated split shipment/receipt document
	  */
	public boolean isPrepareSplitDocument()
	{
		Object oo = get_Value(COLUMNNAME_IsPrepareSplitDocument);
		if (oo != null)
		{
			 if (oo instanceof Boolean)
				 return ((Boolean)oo).booleanValue();
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Sales Transaction.
		@param IsSOTrx This is a Sales Transaction
	*/
	public void setIsSOTrx (boolean IsSOTrx)
	{
		set_Value (COLUMNNAME_IsSOTrx, Boolean.valueOf(IsSOTrx));
	}

	/** Get Sales Transaction.
		@return This is a Sales Transaction
	  */
	public boolean isSOTrx()
	{
		Object oo = get_Value(COLUMNNAME_IsSOTrx);
		if (oo != null)
		{
			 if (oo instanceof Boolean)
				 return ((Boolean)oo).booleanValue();
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Ship/Receipt Confirmation.
		@param IsShipConfirm Require Ship or Receipt Confirmation before processing
	*/
	public void setIsShipConfirm (boolean IsShipConfirm)
	{
		set_Value (COLUMNNAME_IsShipConfirm, Boolean.valueOf(IsShipConfirm));
	}

	/** Get Ship/Receipt Confirmation.
		@return Require Ship or Receipt Confirmation before processing
	  */
	public boolean isShipConfirm()
	{
		Object oo = get_Value(COLUMNNAME_IsShipConfirm);
		if (oo != null)
		{
			 if (oo instanceof Boolean)
				 return ((Boolean)oo).booleanValue();
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Split when Difference.
		@param IsSplitWhenDifference Split document when there is a difference
	*/
	public void setIsSplitWhenDifference (boolean IsSplitWhenDifference)
	{
		set_Value (COLUMNNAME_IsSplitWhenDifference, Boolean.valueOf(IsSplitWhenDifference));
	}

	/** Get Split when Difference.
		@return Split document when there is a difference
	  */
	public boolean isSplitWhenDifference()
	{
		Object oo = get_Value(COLUMNNAME_IsSplitWhenDifference);
		if (oo != null)
		{
			 if (oo instanceof Boolean)
				 return ((Boolean)oo).booleanValue();
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Name.
		@param Name Alphanumeric identifier of the entity
	*/
	public void setName (String Name)
	{
		set_Value (COLUMNNAME_Name, Name);
	}

	/** Get Name.
		@return Alphanumeric identifier of the entity
	  */
	public String getName()
	{
		return (String)get_Value(COLUMNNAME_Name);
	}

    /** Get Record ID/ColumnName
        @return ID/ColumnName pair
      */
    public KeyNamePair getKeyNamePair()
    {
        return new KeyNamePair(get_ID(), getName());
    }

	/** Set Print Text.
		@param PrintName The label text to be printed on a document or correspondence.
	*/
	public void setPrintName (String PrintName)
	{
		set_Value (COLUMNNAME_PrintName, PrintName);
	}

	/** Get Print Text.
		@return The label text to be printed on a document or correspondence.
	  */
	public String getPrintName()
	{
		return (String)get_Value(COLUMNNAME_PrintName);
	}
}