/*
 *     Astatine is a mod for Minecraft which fixes many common bugs in the game
 *     Copyright (C) 2021 Conor Byrne
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.cbyrne.astatine.gui.button;

import dev.cbyrne.astatine.audio.AudioManager;
import dev.cbyrne.astatine.util.ArrayUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.List;

/**
 * A button to cycle between available audio devices
 * See [SoundOptionsScreenMixin]
 *
 * @author Conor Byrne
 */
public class AudioSwitcherButton extends ButtonWidget {
    private final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
    private final ButtonWidget.TooltipSupplier tooltipSupplier;

    private String currentDevice = AudioManager.Configuration.SELECTED_SOUND_DEVICE;

    public AudioSwitcherButton(int x, int y, int width, int height) {
        super(x, y, width, height, Text.of(""), null);

        this.setMessage(getReadableAudioDeviceString(currentDevice));
        this.tooltipSupplier = this::onTooltip;
    }

    /**
     * Gets the next sound device in the list and sets the current device to it
     */
    public void onPress() {
        List<String> devices = AudioManager.getInstance().devices;
        String nextDevice = ArrayUtil.getNext(devices, currentDevice);
        if (!nextDevice.equals(currentDevice)) setCurrentDevice(nextDevice);

        // Play the button press sound on the new sound device
        this.playDownSound(MinecraftClient.getInstance().getSoundManager());
    }

    /**
     * Renders the full audio device name when hovered
     */
    public void renderToolTip(MatrixStack matrices, int mouseX, int mouseY) {
        this.tooltipSupplier.onTooltip(this, matrices, mouseX, mouseY);
    }

    /**
     * Sets the current device.
     * The string's text and the AudioManager configuration will be updated to reflect this change.
     * The sound system is restarted after this change is performed.
     *
     * @param newDevice The name of the device to switch to
     */
    private void setCurrentDevice(String newDevice) {
        currentDevice = newDevice;
        AudioManager.Configuration.SELECTED_SOUND_DEVICE = newDevice;

        this.setMessage(getReadableAudioDeviceString(newDevice));
        AudioManager.getInstance().restartSoundSystem();
    }

    /**
     * Gets a readable string from a sound device name (i.e. OpenAL Soft on Realtek(r) Audio)
     * The string will have "OpenAL Soft on " removed, then trimmed if too long
     *
     * @param device The audio device name
     * @return A readable string for the audio device
     */
    private Text getReadableAudioDeviceString(String device) {
        String originalText = "Audio Device: " + device.replace("OpenAL Soft on ", "");
        String trimmedText = textRenderer.trimToWidth(originalText, 130);

        if (textRenderer.getWidth(originalText) >= 130) {
            trimmedText = trimmedText.replaceAll("\\s+$", "") + "...";
        }

        return Text.of(trimmedText);
    }

    /**
     * Renders a tooltip of the original audio device name if it exceeds the length
     */
    private void onTooltip(ButtonWidget button, MatrixStack matrices, int mouseX, int mouseY) {
        Screen currentScreen = MinecraftClient.getInstance().currentScreen;
        if (currentScreen == null) return;

        String currentDeviceText = getReadableAudioDeviceString(currentDevice).asString();
        if (textRenderer.getWidth(currentDeviceText) >= 130)
            currentScreen.renderTooltip(matrices, Text.of(currentDevice.replace("OpenAL Soft on ", "")), mouseX, mouseY);
    }
}
