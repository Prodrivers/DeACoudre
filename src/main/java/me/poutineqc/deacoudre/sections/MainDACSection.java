package me.poutineqc.deacoudre.sections;

import fr.prodrivers.bukkit.commons.sections.Section;
import fr.prodrivers.bukkit.commons.sections.SectionCapabilities;
import fr.prodrivers.bukkit.commons.ui.section.SelectionUI;
import me.poutineqc.deacoudre.ui.JoinGUI;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;

@Singleton
public class MainDACSection extends Section {
	public static final String DAC_SECTION_NAME = "dac";

	private final JoinGUI joinGUI;

	private static final Set<SectionCapabilities> CAPABILITIES = new HashSet<>();

	static {
		CAPABILITIES.add(SectionCapabilities.CUSTOM_SELECTION_UI);
		CAPABILITIES.add(SectionCapabilities.TRANSITIVE);
	}

	@Inject
	public MainDACSection(JoinGUI joinGUI) {
		super(DAC_SECTION_NAME);
		this.joinGUI = joinGUI;
	}

	@Override
	public @NonNull Set<SectionCapabilities> getCapabilities() {
		return CAPABILITIES;
	}

	@Override
	public boolean preJoin(@NonNull Player player, Section section, boolean b) {
		return true;
	}

	@Override
	public boolean join(@NonNull Player player) {
		return true;
	}

	@Override
	public boolean preLeave(@NonNull OfflinePlayer offlinePlayer, Section section, boolean b) {
		return true;
	}

	@Override
	public boolean leave(@NonNull OfflinePlayer offlinePlayer) {
		return true;
	}

	@Override
	protected SelectionUI getSelectionUI() {
		return this.joinGUI;
	}
}
