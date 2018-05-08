/*******************************************************************************
 * © 2018 Disney | ABC Television Group
 *
 * Licensed under the Apache License, Version 2.0 (the "Apache License")
 * with the following modification; you may not use this file except in
 * compliance with the Apache License and the following modification to it:
 * Section 6. Trademarks. is deleted and replaced with:
 *
 * 6. Trademarks. This License does not grant permission to use the trade
 *     names, trademarks, service marks, or product names of the Licensor
 *     and its affiliates, except as required to comply with Section 4(c) of
 *     the License and to reproduce the content of the NOTICE file.
 *
 * You may obtain a copy of the Apache License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Apache License with the above modification is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the Apache License for the specific
 * language governing permissions and limitations under the Apache License.
 *******************************************************************************/
package com.disney.groovity.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.disney.groovity.model.Model;
import com.disney.groovity.model.ModelFilter;
/**
 * Capture loosely structured data and associated attachments for storage
 *
 * @author Alex Vigdor
 */
public class StorePayload {
	private Object data;
	private List<Attachment> attachmentsToStore = new ArrayList<>();
	private List<String> attachmentsToDelete = new ArrayList<>();
	
	public StorePayload(Model m, List<ModelFilter> filters, Object previous) throws Exception{
		AttachmentCollector ac = new AttachmentCollector();
		ac.setFilters(filters.toArray(new ModelFilter[0]));
		ac.visit(m);
		Map<String, Attachment> newAttachments = ac.getAttachments();
		data = ac.getCollected();
		if(previous != null) {
			AttachmentCollector pc = new AttachmentCollector();
			pc.visit(previous);
			Map<String, Attachment> oldAttachments = pc.getAttachments();
			for(Iterator<Entry<String, Attachment>> iter = newAttachments.entrySet().iterator(); iter.hasNext();) {
				Entry<String, Attachment> newAttachmentEntry = iter.next();
				Attachment oldAttachment = oldAttachments.remove(newAttachmentEntry.getKey());
				if(oldAttachment!=null && oldAttachment.getMd5()!=null && newAttachmentEntry.getValue().getMd5().equals(oldAttachment.getMd5())) {
					//hashes match, we'll skip storing the binary payload
					iter.remove();
				}
			}
			//any remaining old attachment keys should be deleted
			attachmentsToDelete.addAll(oldAttachments.keySet());
		}
		//store all new attachments that weren't reconciled out, and that are real attachment implementations
		newAttachments.values().stream().filter(a->{return !a.getClass().equals(Attachment.class);}).forEach(attachmentsToStore::add);
	}
	
	public Object getData() {
		return data;
	}

	public List<Attachment> getAttachmentsToStore() {
		return attachmentsToStore;
	}

	public List<String> getAttachmentsToDelete() {
		return attachmentsToDelete;
	}

}
