package com.nodiumhosting.vaultmapper.mixin;

import iskallia.vault.client.gui.framework.element.ContainerElement;
import iskallia.vault.client.gui.framework.element.spi.IElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = ContainerElement.class, remap = false)
public interface InvokerContainerElement {
    @Invoker("addElement")
    <T extends IElement> T invokeAddElement(T element);

}
