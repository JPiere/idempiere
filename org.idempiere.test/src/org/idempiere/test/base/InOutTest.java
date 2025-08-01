/***********************************************************************
 * This file is part of iDempiere ERP Open Source                      *
 * http://www.idempiere.org                                            *
 *                                                                     *
 * Copyright (C) Contributors                                          *
 *                                                                     *
 * This program is free software; you can redistribute it and/or       *
 * modify it under the terms of the GNU General Public License         *
 * as published by the Free Software Foundation; either version 2      *
 * of the License, or (at your option) any later version.              *
 *                                                                     *
 * This program is distributed in the hope that it will be useful,     *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of      *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the        *
 * GNU General Public License for more details.                        *
 *                                                                     *
 * You should have received a copy of the GNU General Public License   *
 * along with this program; if not, write to the Free Software         *
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,          *
 * MA 02110-1301, USA.                                                 *
 *                                                                     *
 * Contributors:                                                       *
 * - etantg                         								   *
 **********************************************************************/
package org.idempiere.test.base;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.compiere.acct.Doc;
import org.compiere.acct.DocManager;
import org.compiere.model.MAccount;
import org.compiere.model.MAcctSchema;
import org.compiere.model.MBPartner;
import org.compiere.model.MClient;
import org.compiere.model.MConversionRate;
import org.compiere.model.MCurrency;
import org.compiere.model.MFactAcct;
import org.compiere.model.MInOut;
import org.compiere.model.MInOutLine;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MPriceList;
import org.compiere.model.MPriceListVersion;
import org.compiere.model.MProduct;
import org.compiere.model.MProductPrice;
import org.compiere.model.MRMA;
import org.compiere.model.MRMALine;
import org.compiere.model.MShipper;
import org.compiere.model.MShippingProcessor;
import org.compiere.model.MWarehouse;
import org.compiere.model.PO;
import org.compiere.model.ProductCost;
import org.compiere.model.Query;
import org.compiere.model.SystemIDs;
import org.compiere.model.X_C_BP_ShippingAcct;
import org.compiere.model.X_M_ShippingProcessorCfg;
import org.compiere.process.DocAction;
import org.compiere.process.DocumentEngine;
import org.compiere.process.ProcessInfo;
import org.compiere.util.Env;
import org.compiere.util.TimeUtil;
import org.compiere.wf.MWorkflow;
import org.idempiere.test.AbstractTestCase;
import org.idempiere.test.ConversionRateHelper;
import org.idempiere.test.DictionaryIDs;
import org.idempiere.test.FactAcct;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

/**
 * @author etantg
 */
public class InOutTest extends AbstractTestCase {
	
	public InOutTest() {
	}
	
	@Test
	/**
	 * https://idempiere.atlassian.net/browse/IDEMPIERE-4656
	 */
	public void testMatReceiptPosting() {
		MBPartner bpartner = MBPartner.get(Env.getCtx(), DictionaryIDs.C_BPartner.TREE_FARM.id); // Tree Farm Inc.
		MProduct product = MProduct.get(Env.getCtx(), DictionaryIDs.M_Product.ELM.id); // Elm Tree
		Timestamp currentDate = TimeUtil.getDay(Env.getContextAsDate(Env.getCtx(), "#Date"));
		
		int Spot_ConversionType_ID = DictionaryIDs.C_ConversionType.SPOT.id; // Spot;
		int Company_ConversionType_ID = DictionaryIDs.C_ConversionType.COMPANY.id; // Company
		
		MPriceList priceList = new MPriceList(Env.getCtx(), 0, getTrxName());
		priceList.setName("Purchase AUD " + System.currentTimeMillis());
		MCurrency australianDollar = MCurrency.get(DictionaryIDs.C_Currency.AUD.id); // Australian Dollar (AUD)
		priceList.setC_Currency_ID(australianDollar.getC_Currency_ID());
		priceList.setPricePrecision(australianDollar.getStdPrecision());
		priceList.saveEx();
		
		MPriceListVersion plv = new MPriceListVersion(priceList);
		plv.setM_DiscountSchema_ID(DictionaryIDs.M_DiscountSchema.PURCHASE_2001.id); // Purchase 2001
		plv.setValidFrom(currentDate);
		plv.saveEx();
		
		BigDecimal priceInAud = new BigDecimal(23.32);
		MProductPrice pp = new MProductPrice(plv, product.getM_Product_ID(), priceInAud, priceInAud, Env.ZERO);
		pp.saveEx();
		
		MCurrency usd = MCurrency.get(DictionaryIDs.C_Currency.USD.id); // USD
		BigDecimal audToUsdCompany = new BigDecimal(0.676234);
		BigDecimal audToUsdSpot = new BigDecimal(0.77);
		
		MCurrency euro = MCurrency.get(DictionaryIDs.C_Currency.EUR.id); // EUR
		BigDecimal audToEuroCompany = new BigDecimal(0.746234);
		BigDecimal audToEuroSpot = new BigDecimal(0.64);

		try (MockedStatic<MConversionRate> conversionRateMock = ConversionRateHelper.mockStatic();
			 MockedStatic<MPriceList> priceListMock = mockStatic(MPriceList.class)) {
			mockGetRate(conversionRateMock, australianDollar, usd, Company_ConversionType_ID, currentDate, audToUsdCompany);
			mockGetRate(conversionRateMock, australianDollar, usd, Spot_ConversionType_ID, currentDate, audToUsdSpot);
			mockGetRate(conversionRateMock, australianDollar, euro, Company_ConversionType_ID, currentDate, audToEuroCompany);
			mockGetRate(conversionRateMock, australianDollar, euro, Spot_ConversionType_ID, currentDate, audToEuroSpot);
			
			priceListMock.when(() -> MPriceList.get(any(Properties.class), anyInt(), any())).thenCallRealMethod();
			priceListMock.when(() -> MPriceList.get(any(Properties.class), eq(priceList.get_ID()), any())).thenReturn(priceList);
			
			MOrder order = createPurchaseOrder(bpartner, currentDate, priceList.getM_PriceList_ID(), Company_ConversionType_ID);			
			BigDecimal qtyOrdered = new BigDecimal(500);
			MOrderLine orderLine = createOrderLine(order, 10, product, qtyOrdered, priceInAud);
			completeDocument(order);
			
			MInOut receipt = createMMReceipt(order, currentDate);			
			BigDecimal qtyDelivered = new BigDecimal(500);
			MInOutLine receiptLine = createInOutLine(receipt, orderLine, qtyDelivered);
			completeDocument(receipt);
			postDocument(receipt);
			
			MAcctSchema[] ass = MAcctSchema.getClientAcctSchema(Env.getCtx(), Env.getAD_Client_ID(Env.getCtx()));
			for (MAcctSchema as : ass) {
				BigDecimal rate = Env.ZERO;
				if (as.getC_Currency_ID() == usd.getC_Currency_ID())
					rate = audToUsdCompany;
				else if (as.getC_Currency_ID() == euro.getC_Currency_ID())
					rate = audToEuroCompany;
					
				Doc doc = DocManager.getDocument(as, MInOut.Table_ID, receipt.get_ID(), getTrxName());
				doc.setC_BPartner_ID(receipt.getC_BPartner_ID());
				MAccount acctNIR = doc.getAccount(Doc.ACCTTYPE_NotInvoicedReceipts, as);
				
				BigDecimal acctSource = orderLine.getPriceActual().multiply(receiptLine.getMovementQty())
						.setScale(as.getC_Currency().getStdPrecision(), RoundingMode.HALF_UP);
				BigDecimal acctAmount = acctSource.multiply(rate)
						.setScale(as.getC_Currency().getStdPrecision(), RoundingMode.HALF_UP);
				Query query = MFactAcct.createRecordIdQuery(MInOut.Table_ID, receipt.get_ID(), as.getC_AcctSchema_ID(), getTrxName());
				List<MFactAcct> factAccts = query.list();
				List<FactAcct> expected = Arrays.asList(new FactAcct(acctNIR, acctAmount, acctSource, as.getC_Currency().getStdPrecision(), false, receiptLine.get_ID()));
				assertFactAcctEntries(factAccts, expected);
			}
			
			order = createPurchaseOrder(bpartner, currentDate, priceList.getM_PriceList_ID(), Spot_ConversionType_ID);
			orderLine = createOrderLine(order, 10, product, qtyOrdered, priceInAud);
			completeDocument(order);
			
			receipt = createMMReceipt(order, currentDate);
			receiptLine = createInOutLine(receipt, orderLine, qtyDelivered);
			completeDocument(receipt);
			postDocument(receipt);
			
			for (MAcctSchema as : ass) {
				BigDecimal rate = Env.ZERO;
				if (as.getC_Currency_ID() == usd.getC_Currency_ID())
					rate = audToUsdSpot;
				else if (as.getC_Currency_ID() == euro.getC_Currency_ID())
					rate = audToEuroSpot;
					
				Doc doc = DocManager.getDocument(as, MInOut.Table_ID, receipt.get_ID(), getTrxName());
				doc.setC_BPartner_ID(receipt.getC_BPartner_ID());
				MAccount acctNIR = doc.getAccount(Doc.ACCTTYPE_NotInvoicedReceipts, as);
				
				BigDecimal acctSource = orderLine.getPriceActual().multiply(receiptLine.getMovementQty())
									.setScale(as.getC_Currency().getStdPrecision(), RoundingMode.HALF_UP);
				BigDecimal acctAmount = acctSource.multiply(rate)
						.setScale(as.getC_Currency().getStdPrecision(), RoundingMode.HALF_UP);
				Query query = MFactAcct.createRecordIdQuery(MInOut.Table_ID, receipt.get_ID(), as.getC_AcctSchema_ID(), getTrxName());
				List<MFactAcct> factAccts = query.list();
				List<FactAcct> expected = Arrays.asList(new FactAcct(acctNIR, acctAmount, acctSource, as.getC_Currency().getStdPrecision(), false, receiptLine.get_ID()));
				assertFactAcctEntries(factAccts, expected);
			}
		}		
	}
	
	@Test
	/**
	 * https://idempiere.atlassian.net/browse/IDEMPIERE-4656
	 */
	public void testMatShipmentPosting() {
		MBPartner bpartner = MBPartner.get(Env.getCtx(), DictionaryIDs.C_BPartner.TREE_FARM.id); // Tree Farm Inc.
		MProduct product = MProduct.get(Env.getCtx(), DictionaryIDs.M_Product.ELM.id); // Elm Tree
		Timestamp currentDate = TimeUtil.getDay(Env.getContextAsDate(Env.getCtx(), "#Date"));
		
		int Company_ConversionType_ID = DictionaryIDs.C_ConversionType.COMPANY.id; // Company
		
		MPriceList priceList = new MPriceList(Env.getCtx(), 0, getTrxName());
		priceList.setName("Purchase AUD " + System.currentTimeMillis());
		MCurrency australianDollar = MCurrency.get(DictionaryIDs.C_Currency.AUD.id); // Australian Dollar (AUD)
		priceList.setC_Currency_ID(australianDollar.getC_Currency_ID());
		priceList.setPricePrecision(australianDollar.getStdPrecision());
		priceList.saveEx();
		
		MPriceListVersion plv = new MPriceListVersion(priceList);
		plv.setM_DiscountSchema_ID(DictionaryIDs.M_DiscountSchema.PURCHASE_2001.id); // Purchase 2001
		plv.setValidFrom(currentDate);
		plv.saveEx();
		
		BigDecimal priceInAud = new BigDecimal(23.32);
		MProductPrice pp = new MProductPrice(plv, product.getM_Product_ID(), priceInAud, priceInAud, Env.ZERO);
		pp.saveEx();
		
		MCurrency usd = MCurrency.get(DictionaryIDs.C_Currency.USD.id); // USD
		BigDecimal audToUsdCompany = new BigDecimal(0.676234);
		
		MCurrency euro = MCurrency.get(DictionaryIDs.C_Currency.EUR.id); // EUR
		BigDecimal audToEuroCompany = new BigDecimal(0.746234);
		try (MockedStatic<MConversionRate> conversionRateMock = ConversionRateHelper.mockStatic();
			 MockedStatic<MPriceList> priceListMock = mockStatic(MPriceList.class)) {
			mockGetRate(conversionRateMock, australianDollar, usd, Company_ConversionType_ID, currentDate, audToUsdCompany);
			mockGetRate(conversionRateMock, australianDollar, euro, Company_ConversionType_ID, currentDate, audToEuroCompany);
			
			priceListMock.when(() -> MPriceList.get(any(Properties.class), anyInt(), any())).thenCallRealMethod();
			priceListMock.when(() -> MPriceList.get(any(Properties.class), eq(priceList.get_ID()), any())).thenReturn(priceList);
			
			MOrder order = createPurchaseOrder(bpartner, currentDate, priceList.getM_PriceList_ID(), Company_ConversionType_ID);			
			BigDecimal qtyOrdered = BigDecimal.TEN;
			MOrderLine orderLine = createOrderLine(order, 10, product, qtyOrdered, priceInAud);
			completeDocument(order);
			
			MInOut receipt = createMMReceipt(order, currentDate);			
			BigDecimal qtyDelivered = BigDecimal.TEN;
			MInOutLine receiptLine = createInOutLine(receipt, orderLine, qtyDelivered);
			completeDocument(receipt);
			postDocument(receipt);
			
			MAcctSchema[] ass = MAcctSchema.getClientAcctSchema(Env.getCtx(), Env.getAD_Client_ID(Env.getCtx()));
			for (MAcctSchema as : ass) {
				BigDecimal rate = Env.ZERO;
				if (as.getC_Currency_ID() == usd.getC_Currency_ID())
					rate = audToUsdCompany;
				else if (as.getC_Currency_ID() == euro.getC_Currency_ID())
					rate = audToEuroCompany;
					
				Doc doc = DocManager.getDocument(as, MInOut.Table_ID, receipt.get_ID(), getTrxName());
				doc.setC_BPartner_ID(receipt.getC_BPartner_ID());
				MAccount acctNIR = doc.getAccount(Doc.ACCTTYPE_NotInvoicedReceipts, as);
				
				BigDecimal acctSource = orderLine.getPriceActual().multiply(receiptLine.getMovementQty())
						.setScale(as.getC_Currency().getStdPrecision(), RoundingMode.HALF_UP);
				BigDecimal acctAmount = acctSource.multiply(rate)
						.setScale(as.getC_Currency().getStdPrecision(), RoundingMode.HALF_UP);
				Query query = MFactAcct.createRecordIdQuery(MInOut.Table_ID, receipt.get_ID(), as.getC_AcctSchema_ID(), getTrxName());
				List<MFactAcct> fas = query.list();
				List<FactAcct> expected = Arrays.asList(new FactAcct(acctNIR, acctAmount, acctSource, 2, false, receiptLine.get_ID()));
				assertFactAcctEntries(fas, expected);
			}
			
			MRMA rma = new MRMA(Env.getCtx(), 0, getTrxName());
			rma.setName(order.getDocumentNo());
			rma.setC_DocType_ID(DictionaryIDs.C_DocType.VENDOR_RETURN_MATERIAL.id); // Vendor Return Material
			rma.setM_RMAType_ID(DictionaryIDs.M_RMAType.DAMAGE_ON_ARRIVAL.id); // Damaged on Arrival
			rma.setM_InOut_ID(receipt.get_ID());
			rma.setIsSOTrx(false);
			rma.setSalesRep_ID(SystemIDs.USER_SUPERUSER); // SuperUser
			rma.saveEx();
			
			MRMALine rmaLine = new MRMALine(Env.getCtx(), 0, getTrxName());
			rmaLine.setLine(10);
			rmaLine.setM_RMA_ID(rma.get_ID());
			rmaLine.setM_InOutLine_ID(receiptLine.get_ID());
			rmaLine.setQty(BigDecimal.TEN);
			rmaLine.saveEx();
			
			completeDocument(rma);
			
			MInOut delivery = new MInOut(Env.getCtx(), 0, getTrxName());
			delivery.setM_RMA_ID(rma.get_ID());
			delivery.setBPartner(bpartner);
			delivery.setIsSOTrx(false);
			delivery.setMovementType(MInOut.MOVEMENTTYPE_VendorReturns);
			delivery.setC_DocType_ID(DictionaryIDs.C_DocType.MM_VENDOR_RETURN.id); // MM Vendor Return
			delivery.setDocStatus(DocAction.STATUS_Drafted);
			delivery.setDocAction(DocAction.ACTION_Complete);
			delivery.setM_Warehouse_ID(receipt.getM_Warehouse_ID());
			delivery.saveEx();
			
			MInOutLine deliveryLine = new MInOutLine(delivery);
			deliveryLine.setM_RMALine_ID(rmaLine.get_ID());
			deliveryLine.setLine(10);
			deliveryLine.setProduct(product);
			deliveryLine.setQty(BigDecimal.TEN);
			deliveryLine.setM_Locator_ID(receiptLine.getM_Locator_ID());
			deliveryLine.saveEx();
			
			completeDocument(delivery);
			postDocument(delivery);
			
			for (MAcctSchema as : ass) {
				BigDecimal rate = Env.ZERO;
				if (as.getC_Currency_ID() == usd.getC_Currency_ID())
					rate = audToUsdCompany;
				else if (as.getC_Currency_ID() == euro.getC_Currency_ID())
					rate = audToEuroCompany;
					
				Doc doc = DocManager.getDocument(as, MInOut.Table_ID, delivery.get_ID(), getTrxName());
				doc.setC_BPartner_ID(delivery.getC_BPartner_ID());
				MAccount acctNIR = doc.getAccount(Doc.ACCTTYPE_NotInvoicedReceipts, as);
				
				BigDecimal acctSource = orderLine.getPriceActual().multiply(deliveryLine.getMovementQty())
						.setScale(as.getC_Currency().getStdPrecision(), RoundingMode.HALF_UP);
				BigDecimal acctAmount = acctSource.multiply(rate)
						.setScale(as.getC_Currency().getStdPrecision(), RoundingMode.HALF_UP);
				Query query = MFactAcct.createRecordIdQuery(MInOut.Table_ID, delivery.get_ID(), as.getC_AcctSchema_ID(), getTrxName());
				List<MFactAcct> fas = query.list();
				List<FactAcct> expected = Arrays.asList(new FactAcct(acctNIR, acctAmount, null, 2, true, deliveryLine.get_ID()));
				assertFactAcctEntries(fas, expected);
			}
		}		
	}
	
	private MOrder createPurchaseOrder(MBPartner bpartner, Timestamp date, int M_PriceList_ID, int C_ConversionType_ID)
	{
		 return createOrder(bpartner, date, M_PriceList_ID, C_ConversionType_ID, false);
	}
	
	private MOrder createSalseOrder(MBPartner bpartner, Timestamp date, int M_PriceList_ID, int C_ConversionType_ID)
	{
		return createOrder(bpartner, date, M_PriceList_ID, C_ConversionType_ID, true);
	}

	private MOrder createOrder(MBPartner bpartner, Timestamp date, int M_PriceList_ID, int C_ConversionType_ID, boolean isSOTrx)
	{
		MOrder order = new MOrder(Env.getCtx(), 0, getTrxName());
		order.setAD_Org_ID(DictionaryIDs.AD_Org.HQ.id);
		order.setBPartner(bpartner);
		order.setIsSOTrx(isSOTrx);
		order.setC_DocTypeTarget_ID();
		order.setDateOrdered(date);
		order.setDateAcct(date);
		order.setM_PriceList_ID(M_PriceList_ID);
		order.setC_ConversionType_ID(C_ConversionType_ID);
		order.setM_Warehouse_ID(DictionaryIDs.M_Warehouse.HQ.id);
		order.setDocStatus(DocAction.STATUS_Drafted);
		order.setDocAction(DocAction.ACTION_Complete);
		order.saveEx();
		return order;
	}
	
	private MOrderLine createOrderLine(MOrder order, int line, MProduct product, BigDecimal qty, BigDecimal price) {
		MOrderLine orderLine = new MOrderLine(order);
		orderLine.setLine(line);
		orderLine.setProduct(product);
		orderLine.setQty(qty);
		if (price != null)
			orderLine.setPrice(price);
		else
			orderLine.setPrice();
		orderLine.saveEx();
		return orderLine;
	}
	
	private MInOut createMMReceipt(MOrder order, Timestamp date) {
		MInOut receipt = new MInOut(order, DictionaryIDs.C_DocType.MM_RECEIPT.id, date); // MM Receipt
		receipt.saveEx();
		return receipt;
	}
	
	
	private MInOut createShipment(MOrder order, Timestamp date) {
		MInOut receipt = new MInOut(order, DictionaryIDs.C_DocType.MM_SHIPMENT.id, date); // MM Shipment
		receipt.saveEx();
		return receipt;
	}
	
	private MInOutLine createInOutLine(MInOut mInOut, MOrderLine orderLine, BigDecimal qty) {
		MInOutLine receiptLine = new MInOutLine(mInOut);
		receiptLine.setC_OrderLine_ID(orderLine.get_ID());
		receiptLine.setLine(orderLine.getLine());
		receiptLine.setProduct(orderLine.getProduct());
		receiptLine.setQty(qty);
		MWarehouse wh = MWarehouse.get(Env.getCtx(), mInOut.getM_Warehouse_ID());
		int M_Locator_ID = wh.getDefaultLocator().getM_Locator_ID();
		receiptLine.setM_Locator_ID(M_Locator_ID);
		receiptLine.saveEx();
		return receiptLine;
	}
	
	private void completeDocument(PO po) {
		ProcessInfo info = MWorkflow.runDocumentActionWorkflow(po, DocAction.ACTION_Complete);
		po.load(getTrxName());
		assertFalse(info.isError(), info.getSummary());
		String docStatus = (String) po.get_Value("DocStatus");
		assertEquals(DocAction.STATUS_Completed, docStatus, DocAction.STATUS_Completed + " != " + docStatus);
	}
	
	private void postDocument(PO po) {
		if (!po.get_ValueAsBoolean("Posted")) {
			String error = DocumentEngine.postImmediate(Env.getCtx(), po.getAD_Client_ID(), po.get_Table_ID(), po.get_ID(), false, getTrxName());
			assertTrue(error == null, error);
		}
		po.load(getTrxName());
		assertTrue(po.get_ValueAsBoolean("Posted"));
	}
	
	private void repostDocument(PO po) {
		if (po.get_ValueAsBoolean("Posted")) {
			String error = DocumentEngine.postImmediate(Env.getCtx(), po.getAD_Client_ID(), po.get_Table_ID(), po.get_ID(), false, getTrxName());
			assertTrue(error == null, error);
		}
		po.load(getTrxName());
		assertTrue(po.get_ValueAsBoolean("Posted"));
	}
	
	@Test
	public void testFreightCostRuleCustomerAccount() {
		MOrder order = new MOrder(Env.getCtx(), 0, getTrxName());
		order.setBPartner(MBPartner.get(Env.getCtx(), DictionaryIDs.C_BPartner.JOE_BLOCK.id));
		order.setC_DocTypeTarget_ID(MOrder.DocSubTypeSO_Standard);
		order.setDeliveryRule(MOrder.DELIVERYRULE_CompleteOrder);
		order.setDocStatus(DocAction.STATUS_Drafted);
		order.setDocAction(DocAction.ACTION_Complete);
		Timestamp today = TimeUtil.getDay(System.currentTimeMillis());
		order.setDateOrdered(today);
		order.setDatePromised(today);
		order.saveEx();
		
		X_M_ShippingProcessorCfg cfg = new X_M_ShippingProcessorCfg(Env.getCtx(), 0, getTrxName());
		cfg.setHostAddress("0.0.0.0");
		cfg.setName("Test Shipping Processor Config");
		cfg.setHostPort(0);
		cfg.saveEx();
		
		MShippingProcessor processor = new MShippingProcessor(Env.getCtx(), 0, getTrxName());
		processor.setM_ShippingProcessorCfg_ID(cfg.get_ID());
		processor.setUserID("-");
		processor.setConnectionPassword("-");		
		processor.setName("Test Shipping Processor");
		processor.saveEx();

		MShipper shipper = new MShipper(Env.getCtx(), 0, getTrxName());
		shipper.setName("Test Shipper");
		shipper.setM_ShipperCfg_ID(cfg.get_ID());
		shipper.setM_ShippingProcessor_ID(processor.get_ID());
		shipper.saveEx();
		
		final String shipperAccount = "testFreightCostRuleCustomerAccount";
		
		MBPartner bp = new MBPartner(Env.getCtx(), DictionaryIDs.C_BPartner.JOE_BLOCK.id, getTrxName());
		X_C_BP_ShippingAcct acct = new X_C_BP_ShippingAcct(Env.getCtx(), 0, getTrxName());
		acct.setC_BPartner_ID(bp.getC_BPartner_ID());		
		acct.setShipperAccount(shipperAccount);
		acct.setM_ShippingProcessor_ID(processor.get_ID());
		acct.saveEx();
		
		MInOut inout = new MInOut(Env.getCtx(), 0, getTrxName());				
		inout.setBPartner(bp);
		inout.setIsSOTrx(true);
		inout.setC_Order_ID(order.getC_Order_ID());
		inout.setM_Warehouse_ID(getM_Warehouse_ID());
		inout.setC_DocType_ID();
		inout.setDeliveryViaRule(MInOut.DELIVERYVIARULE_Shipper);
		inout.setM_Shipper_ID(shipper.get_ID());
		inout.setFreightCostRule(MInOut.FREIGHTCOSTRULE_CustomerAccount);
		inout.saveEx();
		
		assertEquals(shipperAccount, inout.getShipperAccount(), "Unexpected shipper account");
		assertEquals(MInOut.FREIGHTCHARGES_Collect, inout.getFreightCharges(), "Unexpected freight charges rule");
	}
	
	/**
	 * Test cases for Credit Check
	 */
	@Test
	public void testCreditCheckInOut()
	{
		MBPartner bpartner = MBPartner.get(Env.getCtx(), DictionaryIDs.C_BPartner.TREE_FARM.id, getTrxName());
		bpartner.setSOCreditStatus(MBPartner.SOCREDITSTATUS_NoCreditCheck);
		bpartner.saveEx();

		MProduct product = MProduct.get(Env.getCtx(), DictionaryIDs.M_Product.ELM.id);
		Timestamp currentDate = Env.getContextAsDate(Env.getCtx(), "#Date");

		MOrder order = createSalseOrder(bpartner, currentDate, DictionaryIDs.M_PriceList.STANDARD.id, DictionaryIDs.C_ConversionType.COMPANY.id);
		MOrderLine orderLine = createOrderLine(order, 10, product, new BigDecimal(500), new BigDecimal(23.32));
		completeDocument(order);

		MInOut receipt = createShipment(order, currentDate);
		BigDecimal qtyDelivered = new BigDecimal(500);
		createInOutLine(receipt, orderLine, qtyDelivered);

		ProcessInfo info = MWorkflow.runDocumentActionWorkflow(receipt, DocAction.ACTION_Prepare);
		receipt.load(getTrxName());
		assertFalse(info.isError(), info.getSummary());
		assertEquals(DocAction.STATUS_InProgress, receipt.getDocStatus());

		bpartner.setSOCreditStatus(MBPartner.SOCREDITSTATUS_CreditStop);
		bpartner.saveEx();

		receipt.load(getTrxName());
		info = MWorkflow.runDocumentActionWorkflow(receipt, DocAction.ACTION_Prepare);
		assertTrue(info.isError(), info.getSummary());
		assertEquals(DocAction.STATUS_Invalid, receipt.getDocStatus());

		bpartner.setSOCreditStatus(MBPartner.SOCREDITSTATUS_CreditHold);
		bpartner.saveEx();

		info = MWorkflow.runDocumentActionWorkflow(receipt, DocAction.ACTION_Prepare);
		assertTrue(info.isError(), info.getSummary());
		assertEquals(DocAction.STATUS_Invalid, receipt.getDocStatus());
	}
	
	@Test
	/**
	 * https://idempiere.atlassian.net/browse/IDEMPIERE-5503
	 */
	public void testShipmentRePosting() {
		MBPartner bpartner = MBPartner.get(Env.getCtx(), DictionaryIDs.C_BPartner.JOE_BLOCK.id);
		MProduct product = MProduct.get(Env.getCtx(), DictionaryIDs.M_Product.AZALEA_BUSH.id);
		Timestamp currentDate = Env.getContextAsDate(Env.getCtx(), "#Date");
		
		// make sure there's cost for AZALEA_BUSH
		MBPartner vendor = MBPartner.get(Env.getCtx(), DictionaryIDs.C_BPartner.SEED_FARM.id);
		MOrder purchaseOrder = createPurchaseOrder(vendor, currentDate, DictionaryIDs.M_PriceList.PURCHASE.id, DictionaryIDs.C_ConversionType.SPOT.id);
		MOrderLine poLine = createOrderLine(purchaseOrder, 10, product, new BigDecimal("1"), null);
		completeDocument(purchaseOrder);
		MInOut receipt = createMMReceipt(purchaseOrder, currentDate);
		createInOutLine(receipt, poLine, new BigDecimal("1"));
		completeDocument(receipt);
		
		MOrder order = createSalseOrder(bpartner, currentDate, DictionaryIDs.M_PriceList.STANDARD.id, DictionaryIDs.C_ConversionType.SPOT.id);
		int plv = MPriceList.get(DictionaryIDs.M_PriceList.STANDARD.id).getPriceListVersion(currentDate).get_ID();
		BigDecimal price = MProductPrice.get(Env.getCtx(), plv, product.get_ID(), getTrxName()).getPriceStd();
		MOrderLine orderLine = createOrderLine(order, 10, product, new BigDecimal("1"), price);
		completeDocument(order);
		
		MInOut delivery = createShipment(order, currentDate);
					
		MInOutLine deliveryLine = createInOutLine(delivery, orderLine, new BigDecimal("1"));
		completeDocument(delivery);
		postDocument(delivery);
		
		ProductCost pc = new ProductCost(Env.getCtx(), deliveryLine.getM_Product_ID(), deliveryLine.getM_AttributeSetInstance_ID(), getTrxName());
		MAcctSchema as = MClient.get(Env.getCtx()).getAcctSchema();
		MAccount cogs = pc.getAccount(ProductCost.ACCTTYPE_P_Cogs, as);
		MAccount asset = pc.getAccount(ProductCost.ACCTTYPE_P_Asset, as);
			
		Query query = MFactAcct.createRecordIdQuery(MInOut.Table_ID, delivery.get_ID(), as.getC_AcctSchema_ID(), getTrxName());
		List<MFactAcct> fas = query.list();
		assertTrue(fas.size() > 0, "Failed to retrieve fact posting entries for shipment document");
		boolean cogsFound = false;
		boolean assetFound = false;
		for (MFactAcct fa : fas) {
			if (cogs.getAccount_ID() == fa.getAccount_ID()) {
				if (deliveryLine.get_ID() == fa.getLine_ID()) {
					assertEquals(fa.getAmtSourceDr().abs().toPlainString(), fa.getAmtSourceDr().toPlainString(), "Not DR COGS");
					assertTrue(fa.getAmtSourceDr().signum() > 0, "Not DR COGS");
				}
				cogsFound = true;
			} else if (asset.getAccount_ID() == fa.getAccount_ID()) {
				if (deliveryLine.get_ID() == fa.getLine_ID()) {
					assertEquals(fa.getAmtSourceCr().abs().toPlainString(), fa.getAmtSourceCr().toPlainString(), "Not CR Product Asset");
					assertTrue(fa.getAmtSourceCr().signum() > 0, "Not CR Product Asset");
				}
				assetFound = true;
			}
		}
		assertTrue(cogsFound, "No COGS posting found");
		assertTrue(assetFound, "No Product Asset posting found");
		
		//re-post
		repostDocument(delivery);
		fas = query.list();
		cogsFound = false;
		assetFound = false;
		for (MFactAcct fa : fas) {
			if (cogs.getAccount_ID() == fa.getAccount_ID()) {
				if (deliveryLine.get_ID() == fa.getLine_ID()) {
					assertEquals(fa.getAmtSourceDr().abs().toPlainString(), fa.getAmtSourceDr().toPlainString(), "Not DR COGS");
					assertTrue(fa.getAmtSourceDr().signum() > 0, "Not DR COGS");
				}
				cogsFound = true;
			} else if (asset.getAccount_ID() == fa.getAccount_ID()) {
				if (deliveryLine.get_ID() == fa.getLine_ID()) {
					assertEquals(fa.getAmtSourceCr().abs().toPlainString(), fa.getAmtSourceCr().toPlainString(), "Not CR Product Asset");
					assertTrue(fa.getAmtSourceCr().signum() > 0, "Not CR Product Asset");
				}
				assetFound = true;
			}
		}
		assertTrue(cogsFound, "No COGS posting found");
		assertTrue(assetFound, "No Product Asset posting found");
	}
	
	private void mockGetRate(MockedStatic<MConversionRate> conversionRateMock, MCurrency fromCurrency,
			MCurrency toCurrency, int C_ConversionType_ID, Timestamp conversionDate, BigDecimal multiplyRate) {
		ConversionRateHelper.mockGetRate(conversionRateMock, fromCurrency, toCurrency, C_ConversionType_ID, 
				conversionDate, multiplyRate, getAD_Client_ID(), getAD_Org_ID());
		ConversionRateHelper.mockGetRate(conversionRateMock, toCurrency, fromCurrency, C_ConversionType_ID, 
				conversionDate, BigDecimal.valueOf(1d/multiplyRate.doubleValue()), getAD_Client_ID(), getAD_Org_ID());
	}
}
