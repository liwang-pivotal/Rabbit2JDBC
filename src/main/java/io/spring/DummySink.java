package io.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.jdbc.core.JdbcTemplate;

@EnableBinding(Sink.class)
public class DummySink {
	
	@Autowired
	JdbcTemplate jdbcTemplate;
	
	String sql = "INSERT INTO test (msg) VALUES (?)";
	
	@StreamListener(Sink.INPUT)
    public void log(String message) {	
		insertHandoffDetails(transactionsDetails);
    }

	public ResponseEntity<?> insertHandoffDetails(TransactionDetails transactionDetails) {
		
		logger.info("insertHandoffDetails .... Started");
		Map<String, Object> result = new HashMap<String, Object>();
		              
		if(mstrDealerNameCode == null) {
			mstrDealerNameCode = getMasterDealerCode();
		}
		              
        try{
                     //Setting values to HandoffPaymentData to match SQL type so that SqlStructValue class will do the mapping between the bean properties of the HandoffPaymentData class and the attributes of the STRUCT
        	HandoffPaymentData paymentData = getHandoffPaymentDataValue(transactionDetails);
 
            List<Items> itemList = new ArrayList<Items>();
                     if(transactionDetails.getSubscriberDetails() !=null){
                           for(SubscriberDetails sd : transactionDetails.getSubscriberDetails()){
                                  if(sd !=null && sd.getItemDetails() != null && !sd.getItemDetails().isEmpty()){
                                         for(ItemDetails itemDetails : sd.getItemDetails()){
                                                Items it = new Items();
                                                if(sd.getPhoneNumber() !=null && !sd.getPhoneNumber().isEmpty()){
                                                       it.setMsisdn(Long.valueOf(sd.getPhoneNumber()));
                                                }
                                                if(itemDetails.getEquipmentId() !=null && !itemDetails.getEquipmentId().isEmpty()){
                                                       it.setEquipment_id(Long.valueOf(itemDetails.getEquipmentId()));
                                                }
                                                it.setItem_amount(itemDetails.getItemAmount());
                                                it.setItem_code(itemDetails.getItemCode());
                                                if(itemDetails.getItemTax() !=null && !itemDetails.getItemTax().isEmpty()){
                                                       it.setItem_tax(Double.parseDouble(itemDetails.getItemTax()));
                                                }
                                                it.setItem_type(itemDetails.getItemType());
                                                it.setImei(itemDetails.getImei());
                                                it.setMsrp(itemDetails.getMsrp());
                                                it.setItem_description(itemDetails.getItemDescription());
                                                it.setTransaction_status_id((long) 1);
                                               
                                                /*if(itemDetails.getItemAmountWithTax() != null && !itemDetails.getItemAmountWithTax().isEmpty()){
                                                       it.setItem_amount_with_tax(Double.parseDouble(itemDetails.getItemAmountWithTax()));
                                                }*/
                                                if(sd.getTotalItemAmountWithoutTax() !=null && !sd.getTotalItemAmountWithoutTax().isEmpty()){
                                                       it.setTotal_item_amount_without_tax(Double.parseDouble(sd.getTotalItemAmountWithoutTax()));
                                                }
                                                if(sd.getTotalTax() !=null && !sd.getTotalTax().isEmpty()){
                                                       it.setTotal_tax(Double.parseDouble(sd.getTotalTax()));
                                                }
                                                if(sd.getTotalItemAmountWithTax() !=null && !sd.getTotalItemAmountWithTax().isEmpty()){
                                                       it.setTotalitem_amount_with_tax(Double.parseDouble(sd.getTotalItemAmountWithTax()));
                                                }
                                               
                                                if(itemDetails.getEffectiveDate() !=null){
                                                       //java.sql.Date sqlDate = new java.sql.Date(convertDate(itemDetails.getEffectiveDate()).getTime());
                                                       java.sql.Timestamp sqlDate = convertDate(itemDetails.getEffectiveDate());
                                                       it.setEffective_date(sqlDate);
                                                      
                                                }
                                                it.setAuth_status_code(itemDetails.getAuthStatusCode());
                                                it.setAuth_reason_code(itemDetails.getAuthReasonCode());
                                                it.setReason_description(itemDetails.getReasonDescription());
                                                itemList.add(it);
                                         }
                                  }
                           }
                     }
                    
                     Items[] items = new Items[itemList.size()];
                     for(int i=0;i<itemList.size();i++){
                           items[i] = itemList.get(i);
                     }
                     System.out.println("Procedure Call");
                     SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(jdbcTemplate);
                     simpleJdbcCall.withCatalogName("PAYMENTS");
                     simpleJdbcCall.withProcedureName("insertPaymentTransactionData");
                     simpleJdbcCall.setSchemaName(appConfigService.getSpringDatasourceSchema());
                     simpleJdbcCall.declareParameters( new SqlParameters().getInsertHandOffParameters());
                     StructMapper<Items> mapper = new ItemMapper();
                     Map in = new HashMap();
                     in.put("paymentTransactionData", new SqlStructValue<HandoffPaymentData>(paymentData));
                     in.put("items",new SqlStructArrayValue<Items>(items, mapper,"PAYMENTTRANSACTIONITEM_T", "PAYMENTTRANSACTIONITEMS_VAT"));
                     result = simpleJdbcCall.execute(in);
                     result.put("transactionId", result.get("transaction_id"));
                     result.put("paymentAppURL", appConfigService.getPaymentAppUrl());
                     result.put("paymentAppReturnUrl", appConfigService.getPaymentAppReturnUrl());
              }catch(Exception e){
                     e.printStackTrace();
                     logger.info("Exception occured while calling the service" + e.getMessage());
                     //result.put("status", "101");
                     result = getErrorResponse(result, Constants.STATUS_CODE_504,"Exception occured while calling the service");
                     return new ResponseEntity<Object>(result, HttpStatus.OK);
              }finally{
 
              }
              logger.info("insertHandoffDetails Success .. ");
 
              return new ResponseEntity<Object>(result, HttpStatus.OK);
		 
	}
}
