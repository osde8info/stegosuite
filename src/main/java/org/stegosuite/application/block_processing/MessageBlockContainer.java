package org.stegosuite.application.block_processing;

import org.stegosuite.model.payload.block.MessageBlock;

import java.util.List;

class MessageBlockContainer implements BlockContainer {
	private MessageBlock block;
	private List<String> processedMessages;

	public MessageBlockContainer(MessageBlock block, List<String> messages) {
		this.block = block;
		this.processedMessages = messages;
	}

	@Override
	public void processBlock() {
		String message = block.getMessage();
		processedMessages.add(message);
	}
}
