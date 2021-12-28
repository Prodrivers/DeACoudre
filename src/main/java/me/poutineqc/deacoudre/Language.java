package me.poutineqc.deacoudre;

import me.poutineqc.deacoudre.commands.DacCommand;
import me.poutineqc.deacoudre.instances.User;
import me.poutineqc.deacoudre.tools.CaseInsensitiveMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map.Entry;

public class Language {

	private static final HashMap<String, Language> languages = new HashMap<>();
	private static DeACoudre plugin;
	private static Configuration config;
	private static File langFolder;
	public String keyWordGeneralAnd;
	public String keyWordGeneralComma;
	public String languageName;
	public String errorAlreadyInGame;
	public String errorNotInGame;
	public String errorGameStarted;
	public String startAlreadyStarted;
	public String startStopped;
	public String joinStateUnset;
	public String joinStateFull;
	public String joinStateStarted;
	public String joinAsSpectator;
	public String joinGamePlayer;
	public String joinGameOthers;
	public String quitGamePlayer;
	public String quitGameOthers;
	public String gameTimeOutPlayer;
	public String gameTimeOutOthers;
	public String startErrorQuantity;
	public String startBroadcast;
	public Component startRandomColor;
	public String startRandomOrder;
	public String startPosition;
	public String gameTurnPlayer;
	public String gameTurnOthers;
	public String endingBroadcastSingle;
	public String endingStall;
	public String endingBroadcastMultiple;
	public String gameNewRound;
	public String gamePointsUpPlayer;
	public String gamePointsUpOthers;
	public String gamePointsFlushPlayer;
	public String gamePointsFlushOthers;
	public String gamePointsRevivePlayer;
	public String gamePointsReviveOthers;
	public String gamePointsEliminatePlayer;
	public String gamePointsEliminateOthers;
	public String gamePointsDownPlayer;
	public String gamePointsDownOthers;
	public String gamePointsReviveLastLastPlayer;
	public String gamePointsReviveLastLastOthers;
	public String gamePointsReviveLastMultiplePlayer;
	public String gamePointsReviveLastMultipleOthers;
	public String gamePointsConfirmationPlayer;
	public String gamePointsConfirmationOthers;
	public String gameSuccessPlayer;
	public String gameSuccessOthers;
	public String gamePointsReviveHint;
	public String endingRewardMoney;
	public String challengeRewardMoney;
	public String challengeBroadcast;
	public String challengeDisplayPlayed;
	public String challengeDisplayWin;
	public String challengeDisplayLost;
	public String challengeDisplayDaC;
	public String challengeDisplayCompleteArena;
	public String challengeDisplay8PlayersGame;
	public String challengeDisplayReachRound100;
	public String challengeDisplayAnswerToLife;
	public String challengeDisplayFight;
	public String challengeDisplayMinecraftSnail;
	public String keyWordStats;
	public String keyWordChallenges;
	public String keyWordStatsReward;
	public String keyWordStatsGamesPlayed;
	public String keyWordStatsGamesWon;
	public String keyWordStatsGamesLost;
	public String keyWordStatsDacsDone;
	public String keyWordStatsProgression;
	public String keyWordStatsTimePlayed;
	public String keyWordStatsMoneyGot;
	public String keyWordGeneralHours;
	public String keyWordGeneralMinutes;
	public String keyWordGeneralSeconds;
	public String keyWordJumpFast;
	public String keyWordStatsCompleted;
	public String keyWordStatsNotCompleted;
	public String keyWordStatsTop10;
	public String keyWordGuiPreviousPage;
	public String keyWordGuiNextPage;
	public String startCooldown;
	public String errorInGame;
	public String joinGuiTitle;
	public String keyWordGameStateUnset;
	public String keyWordGameStateStarted;
	public String keyWordGameStateFull;
	public String keyWordGameStateReady;
	public String colorGuiTitle;
	public String colorGuiCurrent;
	public String errorArenaNotExist;
	public Component colorChoosen;
	public String colorRandom;
	public String colorAlreadyPicked;
	public String signJoin;
	public String signQuit;
	public String signColor;
	public String signStart;
	public String signStats;
	public String signPlay;
	public String signNotValid1;
	public String signNotValid2;
	public String signNotValid3;
	public String signNoPermission0;
	public String signNoPermission1;
	public String signNoPermission2;
	public String signNoPermission3;
	public String editNewNoName;
	public String editNewSuccess;
	public String editNewExists;
	public String editNewLong;
	public String editErrorNoParameter;
	public String editDeleteSuccess;
	public String editLobbySuccess;
	public String editPlateformSuccess;
	public String editPoolNoSelection;
	public String editPoolSuccess;
	public String editLimitMinSuccess;
	public String editLimitMaxSuccess;
	public String editLimitNaN;
	public String editLimitMinBelowMin;
	public String editLimitErrorMinMax;
	public String editLimitNoParameter;
	public String keyWordColorRandom;
	public String errorCommandNotFound;
	public String errorArenaOrCommandNotFound;
	public String reloadSucess;
	public String keyWordMaterialPrefix;
	public String languageList;
	public String forcestartError;
	public String endingRewardItemsSpaceMultiple;
	public String endingRewardItemsSpaceOne;
	public String endingRewardItemsReceive;
	public String joinGuiTooltip;
	public String editColorGuiTitle;
	public String keyWordGuiInstrictions;
	public String editColorGuiTooltip;
	public String editColorActive;
	public String pluginDevelopper;
	public String pluginVersion;
	public String pluginHelp;
	public String keyWordHelp;
	public String helpDescriptionGeneral;
	public String helpDescriptionGame;
	public String helpDescriptionArena;
	public String helpDescriptionAdmin;
	public String helpDescriptionAll;
	public String keyWordHelpCategory;
	public String errorPermissionHelp;
	public String keyWordHelpPage;
	public String languageNotFound;
	public String languageChangeSuccess;
	public String joinInfoMissingName;
	public String joinInfoTooltip;
	public String keyWordHelpInformation;
	public String keyWordHelpCurrent;
	public String keyWordGameState;
	public String keyWordHelpAmountPlayer;
	public String keyWordGeneralMinimum;
	public String keyWordGeneralMaximum;
	public String keyWordGameStateActive;
	public String keyWordGameStateStartup;
	public String keyWordHelpAdvanced;
	public String keyWordHelpLobby;
	public String keyWordHelpWorld;
	public String keyWordHelpPlateform;
	public String keyWordHelpPool;
	public String editColorColorLessPlayer;
	public String endingTeleport;
	public String endingSimulation;
	public String errorTeleport;
	public String editLimitMaxAboveMax;
	public String keyWordScoreboardPlayers;
	public String keyWordScoreboardPoints;
	public String joinNewPlacePlayer;
	public String joinNewPlaceOthers;
	public String editLimitGameActive;
	public String editColorNoPool;
	public String editErrorNoArena;
	public String editColorChoosen;
	public String keyWordScoreboardRound;
	public String startAutoFail;
	public String convertAlreadyDone;
	public String convertStart;
	public String convertNoMysql;
	public String convertComplete;
	public String prefixLong;
	public String prefixShort;
	public Component prefixShortComponent;
	String errorNoPermission;
	private File languageFile;
	private FileConfiguration languageData;
	private CaseInsensitiveMap commandDescriptions;

	Language(DeACoudre plugin) {
		Language.plugin = plugin;
		config = plugin.getConfiguration();

		langFolder = new File(plugin.getDataFolder(), "LanguageFiles");
		if(!langFolder.exists()) {
			langFolder.mkdir();
		}
	}

	Language(String fileName, boolean forceFileOverwrite) {
		languageFile = new File(langFolder.getPath(), fileName + ".yml");
		if(forceFileOverwrite) {
			languageFile.delete();
			plugin.saveResource("LanguageFiles/" + fileName + ".yml", false);
		}

		if(!languageFile.exists()) {
			InputStream local = plugin.getResource("LanguageFiles/" + fileName + ".yml");
			if(local != null) {
				plugin.saveResource("LanguageFiles/" + fileName + ".yml", false);
			} else {
				Log.info("Could not find " + fileName + ".yml");
			}
		}

		languages.put(fileName, this);
		loadLang();
	}

	public static Entry<String, Language> getLanguage(String languageName) {
		for(Entry<String, Language> local : languages.entrySet()) {
			if(local.getValue().languageName.equalsIgnoreCase(languageName)) {
				return local;
			}
		}

		return null;
	}

	public static HashMap<String, Language> getLanguages() {
		return languages;
	}

	static void clearLanguages() {
		languages.clear();
	}

	public void loadLang() {
		languageData = YamlConfiguration.loadConfiguration(languageFile);

		languageName = languageData.getString("languageName", "english");

		prefixLong = languageData.getString("prefixLong", "&1[&3DeACoudre&1]");
		prefixShort = languageData.getString("prefixShort", "&1[&3DaC&1] ");
		prefixShortComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(prefixShort);

		pluginDevelopper = languageData.getString("pluginDevelopper", "&3Developped by: &7%developper%");
		pluginVersion = languageData.getString("pluginVersion", "&3Version: &7%version%");
		pluginHelp = languageData.getString("pluginHelp", "&3Type &b/%command% help &3 for the list of commands.");

		errorNoPermission = languageData.getString("errorNoPermission", "&cYou don't have the permission to do that!");
		errorPermissionHelp = languageData.getString("errorPermissionHelp", "&cYou do not have any permissions in this category.");
		errorArenaNotExist = languageData.getString("errorArenaNotExist", "&cNot a valid arena name.");
		errorCommandNotFound = languageData.getString("errorCommandNotFound", "&eCommand does not exist &b(&3/dac help &bfor help)&e.");
		errorArenaOrCommandNotFound = languageData.getString("errorArenaOrCommandNotFound", "&eCommand or arena not found &b(&3/dac help &bfor help)&e.");
		errorInGame = languageData.getString("errorInGame", "&cYou can't do that during a game.");
		errorNotInGame = languageData.getString("errorNotInGame", "&cYou are not in a game at the moment.");
		errorAlreadyInGame = languageData.getString("errorAlreadyInGame", "&cYou are already in a game. Do &d/dac quit &cto quit it.");
		errorGameStarted = languageData.getString("errorGameStarted", "&cThe game is already started.");
		errorTeleport = languageData.getString("errorTeleport", "&cYou can't teleport away while in a DeACoudre game.");

		helpDescriptionAll = languageData.getString("helpDescriptionAll", "&7All Commands");
		helpDescriptionGeneral = languageData.getString("helpDescriptionGeneral", "&7General player commands");
		helpDescriptionGame = languageData.getString("helpDescriptionGame", "&7Commands to simply play the game");
		helpDescriptionArena = languageData.getString("helpDescriptionArena", "&7Commands to setup the arenas");
		helpDescriptionAdmin = languageData.getString("helpDescriptionAdmin", "&7Admin maintenance commands");

		languageList = languageData.getString("languageList", "&3Available languages:");
		languageNotFound = languageData.getString("languageNotFound", "&cLanguage not found. &8/%cmd% language &cfor a list of available languages");
		languageChangeSuccess = languageData.getString("languageChangeSuccess", "&aLanguage successfully set to %language%");

		joinGuiTitle = languageData.getString("joinGuiTitle", "&2Arena List &0: &3DeACoudre");
		joinGuiTooltip = languageData.getString("joinGuiTooltip", "&7Click on the arena\n&7you wish to join\n&7Right click to display\n&7it's infos");
		joinStateUnset = languageData.getString("joinStateUnset", "&cThis arena is not ready to use. Ask an admin to finish setting it up.");
		joinStateFull = languageData.getString("joinStateFull", "&cThe arena is full.");
		joinStateStarted = languageData.getString("joinStateStarted", "&cThe game has already started.");
		joinAsSpectator = languageData.getString("joinAsSpectator", "&bJoining the lobby as a spectator.");
		joinGamePlayer = languageData.getString("joinGamePlayer", "&aJoined the lobby from the arena &2%arenaName% &a(&2%amountInGame%&a)");
		joinGameOthers = languageData.getString("joinGameOthers", "&f%player% &3just joined the DaC lobby &a(&2%amountInGame%&a).");
		joinNewPlaceOthers = languageData.getString("joinNewPlaceOthers", "&f%player% &3is added to the game to replace &f%leaver%&3.");
		joinNewPlacePlayer = languageData.getString("joinNewPlacePlayer", "&3You are added to the game to replace &f%leaver%&3!");
		joinInfoMissingName = languageData.getString("joinInfoMissingName", "&cYou need to choose an arena.");
		joinInfoTooltip = languageData.getString("joinInfoTooltip", "&8[&7Tip&8] &7You may also do &8/%cmd% list &7and right click an arena to display it's information.");

		quitGamePlayer = languageData.getString("quitGamePlayer", "&aYou left the DaC game.");
		quitGameOthers = languageData.getString("quitGameOthers", "&f%player% &3left the DaC game.");

		colorGuiTitle = languageData.getString("colorGuiTitle", "&6Choose Color &0: &3DeACoudre");
		colorGuiCurrent = languageData.getString("colorGuiCurrent", "Current Color:");
		colorChoosen = LegacyComponentSerializer.legacyAmpersand().deserialize(
				languageData.getString("colorChoosen", "&dYou have choosen the &f%material% &d: &f%color%&d.")
		);
		colorRandom = languageData.getString("colorRandom", "&dYou let the fate decide of your color.");
		colorAlreadyPicked = languageData.getString("colorAlreadyPicked", "&cThis color has been picked while you were choosing. Sorry, try again.");

		startRandomColor = LegacyComponentSerializer.legacyAmpersand().deserialize(
				languageData.getString("startRandomColor", "&5You were randomly assigned the &f%material% &5: &f%color%&5.")
		);
		startRandomOrder = languageData.getString("startRandomOrder", "&9Random Position Order List:");
		startPosition = languageData.getString("startPosition", "&9%posNo% - &f%player%");
		startAlreadyStarted = languageData.getString("startAlreadyStarted", "&cThe countdown has already been started.");
		startStopped = languageData.getString("startStopped", "&cThere is not enough players to start a game anymore. The countdown has been stopped.");
		startErrorQuantity = languageData.getString("startErrorQuantity", "&cThere must be between &4%minPlayers% &cand &4%maxPlayers% &cplayers to start the game.");
		startCooldown = languageData.getString("startCooldown", "&cYou can't start a game so fast after the last was aborted. Wait 30 seconds.");
		startAutoFail = languageData.getString("startAutoFail", "&cThe counter did not autostart because the last one was aborted less than 30 seconds ago.");
		startBroadcast = languageData.getString("startBroadcast", "&6A new game of DaC will start in %time% seconds in the arena &3%arena%&6. All interested players may do &4/dac join %arena% &6to join the game.");

		gameNewRound = languageData.getString("gameNewRound", "&8Round %round% has started");
		gameTurnPlayer = languageData.getString("gameTurnPlayer", "&d&lIt's your turn to play!");
		gameTurnOthers = languageData.getString("gameTurnOthers", "&dIt's &f%player%&d's turn to play!");
		gameSuccessPlayer = languageData.getString("gameSuccessPlayer", "&dYou successfully jumped");
		gameSuccessOthers = languageData.getString("gameSuccessOthers", "&f%player% &dsuccessfully jumped");
		gamePointsUpPlayer = languageData.getString("gamePointsUpPlayer", "&6Congratuation! &dYou just did a DaC! (&5%points%&d)");
		gamePointsUpOthers = languageData.getString("gamePointsUpOthers", "&f%player% &djust did a DaC! (&5%points%&d)");
		gamePointsDownPlayer = languageData.getString("gamePointsDownPlayer", "&eYou lost a life (&6%points%&e)");
		gamePointsDownOthers = languageData.getString("gamePointsDownOthers", "&f%player% &ehas lost a life (&6%points%&e)");
		gamePointsConfirmationPlayer = languageData.getString("gamePointsConfirmationPlayer", "&dOh dear, you don't have any life left. Waiting for confirmation...");
		gamePointsConfirmationOthers = languageData.getString("gamePointsConfirmationOthers", "&f%player% &dfailed and has no points left. Waiting for confirmation...");
		gamePointsReviveHint = languageData.getString("gamePointsReviveHint", "&8(&7You may get it back if everybody else fails and someone losses his final life this round&8)");
		gamePointsReviveLastLastPlayer = languageData.getString("gamePointsReviveLastLastPlayer", "&dYou lost you last life, but you are given another chance since everybody also failed this round.");
		gamePointsReviveLastLastOthers = languageData.getString("gamePointsReviveLastLastOthers", "&f%player% &dhas lost his last life but is given another chance since everybody else did fail this round..");
		gamePointsReviveLastMultiplePlayer = languageData.getString("gamePointsReviveLastMultiplePlayer", "&dYou failed.. as did everyone else this round and someone lost his last life. Your are given another chance");
		gamePointsReviveLastMultipleOthers = languageData.getString("gamePointsReviveLastMultipleOthers", "&f%player% &dhas failed but is given another chance since everybody else did fail this round and someone lost his last life..");
		gamePointsRevivePlayer = languageData.getString("gamePointsRevivePlayer", "&dYou are also receiving another chance. (&5%points%&d)");
		gamePointsReviveOthers = languageData.getString("gamePointsReviveOthers", "&f%player% &dhas been given another chance. (&5%points%&d)");
		gamePointsEliminatePlayer = languageData.getString("gamePointsEliminatePlayer", "&cOh dear, you don't have any life left. You are now eliminated.");
		gamePointsEliminateOthers = languageData.getString("gamePointsEliminateOthers", "&f%player% &chas been eliminated.");
		gamePointsFlushPlayer = languageData.getString("gamePointsFlushPlayer", "&f%player%&c's success means your loss. You are eliminated.");
		gamePointsFlushOthers = languageData.getString("gamePointsFlushOthers", "&cDue to &f%player%&c's success, &f%looser% &cis now eliminated.");
		gameTimeOutPlayer = languageData.getString("gameTimeOutPlayer", "&cIt took you too long to play. You are now eliminated.");
		gameTimeOutOthers = languageData.getString("gameTimeOutOthers", "&f%player% &ctook too long to play. He is now eliminated.");

		endingBroadcastSingle = languageData.getString("endingBroadcastSingle", "&6Congratulation to &f%player% &6who just won a game of DaC in the arena &3%arenaName%&6.");
		endingStall = languageData.getString("endingStall", "&eThe game got stale since no successful move has been made for &6%time% &emoves.\n&6Calculating who will be the winner... &7(The one with the most lives)");
		endingBroadcastMultiple = languageData.getString("endingBroadcastMultiple", "&6Congratulation to &f%players% &6who just finished completely the arena &3%arenaName%&6.");
		endingRewardMoney = languageData.getString("endingRewardMoney", "&dYou receive &5%currency%%amount% &dfor your win.");
		endingRewardItemsSpaceMultiple = languageData.getString("endingRewardItemsSpaceMultiple", "&cYou don't have place in you inventory for all your rewards.");
		endingRewardItemsSpaceOne = languageData.getString("endingRewardItemsSpaceOne", "&cYou don't have place in you inventory for your reward.");
		endingRewardItemsReceive = languageData.getString("endingRewardItemsReceive", "&aYou win %amount% &f%item% &afor your victory.");
		endingTeleport = languageData.getString("endingTeleport", "&dGame is over. Teleporting back in 5 seconds...");
		endingSimulation = languageData.getString("endingSimulation", "&6The simulation is over!");

		challengeDisplayPlayed = languageData.getString("challengeDisplayPlayed", "Play %amount% game(s)");
		challengeDisplayWin = languageData.getString("challengeDisplayWin", "Win %amount% game(s)");
		challengeDisplayLost = languageData.getString("challengeDisplayLost", "Loose %amount% game(s)");
		challengeDisplayDaC = languageData.getString("challengeDisplayDaC", "Achieve %amount% DaC(s)");
		challengeDisplayCompleteArena = languageData.getString("challengeDisplayCompleteArena", "Complete an Arena");
		challengeDisplay8PlayersGame = languageData.getString("challengeDisplay8PlayersGame", "Play a 8 players game");
		challengeDisplayReachRound100 = languageData.getString("challengeDisplayReachRound100", "Reach round 100");
		challengeDisplayAnswerToLife = languageData.getString("challengeDisplayAnswerToLife", "The answer to life the universe and everything");
		challengeDisplayFight = languageData.getString("challengeDisplayFight", "Fight!");
		challengeDisplayMinecraftSnail = languageData.getString("challengeDisplayMinecraftSnail", "The Minecraft snail");
		challengeRewardMoney = languageData.getString("challengeRewardMoney", "&dYou receive &5%currency%%amount% &dfor the completion of your challenge.");
		challengeBroadcast = languageData.getString("challengeBroadcast", "&f%player% &6just achieved the challenge: &4%challenge%");

		signJoin = languageData.getString("signJoin", "&aJoin Arena");
		signPlay = languageData.getString("signPlay", "&bPlay");
		signQuit = languageData.getString("signQuit", "&cQuit Arena");
		signColor = languageData.getString("signColor", "&6Change Color");
		signStart = languageData.getString("signStart", "&9Start Game");
		signStats = languageData.getString("signStats", "&5Stats");
		signNotValid1 = languageData.getString("signNotValid1", "&cNone valid");
		signNotValid2 = languageData.getString("signNotValid2", "&csign parameters");
		signNotValid3 = languageData.getString("signNotValid3", "&cTry again");
		signNoPermission0 = languageData.getString("signNoPermission0", "&cYou don't have");
		signNoPermission1 = languageData.getString("signNoPermission1", "&cthe permissions");
		signNoPermission2 = languageData.getString("signNoPermission2", "&cto create a DaC");
		signNoPermission3 = languageData.getString("signNoPermission3", "&csign, &4Sorry...");

		editErrorNoArena = languageData.getString("editErrorNoArena", "&cYou must provide an arena name for this command.");
		editErrorNoParameter = languageData.getString("editErrorNoParameter", "&eYou must choose what you want to do with this arena.");
		editNewNoName = languageData.getString("editNewNoName", "&cYou must provide a name for the new arena.");
		editNewExists = languageData.getString("editNewExists", "&cAn arena named &4%arenaName% &calready exists.");
		editNewLong = languageData.getString("editNewLong", "&cThe arena's name can't be more than one word.");
		editNewSuccess = languageData.getString("editNewSuccess", "&aNew arena &2%arenaName% &asuccessfully created.");
		editDeleteSuccess = languageData.getString("editDeleteSuccess", "&aSuccessfully deleted the arena &2%arenaName%");
		editLobbySuccess = languageData.getString("editLobbySuccess", "&aLobby sucessfully set for the arena &2%arenaName%&a.");
		editPlateformSuccess = languageData.getString("editPlateformSuccess", "&aPlateform sucessfully set for the arena &2%arenaName%&a.");
		editPoolNoSelection = languageData.getString("editPoolNoSelection", "&cYou must first make a selection with world edit.");
		editPoolSuccess = languageData.getString("editPoolSuccess", "&aPool sucessfully set for the arena &2%arenaName%&a.");
		editLimitMinSuccess = languageData.getString("editLimitMinSuccess", "&aSuccessfully set to &2%amount% &athe minimum amount of players for the arena &2%arenaName%");
		editLimitMaxSuccess = languageData.getString("editLimitMaxSuccess", "&aSuccessfully set to &2%amount% &athe maximum amount of players for the arena &2%arenaName%");
		editLimitGameActive = languageData.getString("editLimitGameActive", "&cYou can't edit the amount of player while there is a game active");
		editLimitNaN = languageData.getString("editLimitNaN", "&cThe amount must be a natural number.");
		editLimitNoParameter = languageData.getString("editLimitNoParameter", "&cYou must provide a number.");
		editLimitMinBelowMin = languageData.getString("editLimitMinBelowMin", "&cThe min amount can't be below 2");
		editLimitMaxAboveMax = languageData.getString("editLimitMaxAboveMax", "&cThe max amount can't be above 12.");
		editLimitErrorMinMax = languageData.getString("editLimitErrorMinMax", "&cThe max can't be above the min (and vice-versa)");
		editColorGuiTitle = languageData.getString("editColorGuiTitle", "&eEdit Colors &0: &3DeACoudre");
		editColorGuiTooltip = languageData.getString("editColorGuiTooltip", "&eThe enchanted blocks are\n&ethe curently selected ones.\n&eClick a block to\n&eenable or disable it.");
		editColorColorLessPlayer = languageData.getString("editColorColorLessPlayer", "&cCan't have less available colors than max players.");
		editColorNoPool = languageData.getString("editColorNoPool", "&cYou can't edit the colors before the pool has been defined.");
		editColorChoosen = languageData.getString("editColorChoosen", "&cYou can't remove this block right now. It has already been choosen by a player.");
		editColorActive = languageData.getString("editColorActive", "&cYou can't edit the colors while a game is active.");

		reloadSucess = languageData.getString("reloadSucess", "&aDaC has been successfully reloaded.");
		forcestartError = languageData.getString("forcestartError", "&cMust have only one player in a game to forcestart it.");
		convertAlreadyDone = languageData.getString("convertAlreadyDone", "&cThe conversion to mysql has already been done.");
		convertStart = languageData.getString("convertStart", "&aBegining the conversion. This may take a very long time..");
		convertNoMysql = languageData.getString("convertNoMysql", "&cYou must have a mysql connection to do this command.");
		convertComplete = languageData.getString("convertComplete", "&aThe file to mysql conversion is finished!");

		commandDescriptions = new CaseInsensitiveMap();
		for(DacCommand cmd : DacCommand.getCommands()) {
			commandDescriptions.put(cmd.getDescription(),
					languageData.getString(cmd.getDescription(), "&cOops, an Error has occured!"));
		}

		keyWordGeneralAnd = languageData.getString("keyWordGeneralAnd", " &6and &f");
		keyWordGeneralComma = languageData.getString("keyWordGeneralComma", "&6, &f");
		keyWordGeneralMinimum = languageData.getString("keyWordGeneralMinimum", "Minimum");
		keyWordGeneralMaximum = languageData.getString("keyWordGeneralMaximum", "Maximum");
		keyWordGeneralHours = languageData.getString("keyWordGeneralHours", "hours");
		keyWordGeneralMinutes = languageData.getString("keyWordGeneralMinutes", "minutes");
		keyWordGeneralSeconds = languageData.getString("keyWordGeneralSeconds", "seconds");

		keyWordMaterialPrefix = languageData.getString("keyWordMaterialPrefix", "&f");
		keyWordColorRandom = languageData.getString("keyWordColorRandom", "&6R&da&2n&9d&co&3m");

		keyWordGameState = languageData.getString("keyWordGameState", "game state");
		keyWordGameStateStarted = languageData.getString("keyWordGameStateStarted", "&cAlready Started");
		keyWordGameStateFull = languageData.getString("keyWordGameStateFull", "&cArena Full");
		keyWordGameStateReady = languageData.getString("keyWordGameStateReady", "&aReady");
		keyWordGameStateUnset = languageData.getString("keyWordGameStateUnset", "&7Arena Unset");
		keyWordGameStateStartup = languageData.getString("keyWordGameStateStartup", "&9Startup");
		keyWordGameStateActive = languageData.getString("keyWordGameStateActive", "&cActive");

		keyWordChallenges = languageData.getString("keyWordChallenges", "&dChallenges");
		keyWordStats = languageData.getString("keyWordStats", "&5Stats");
		keyWordStatsTop10 = languageData.getString("keyWordStatsTop10", "Top 10");
		keyWordStatsReward = languageData.getString("keyWordStatsReward", "Reward");
		keyWordStatsGamesPlayed = languageData.getString("keyWordStatsGamesPlayed", "Games Played");
		keyWordStatsGamesWon = languageData.getString("keyWordStatsGamesWon", "Games Won");
		keyWordStatsGamesLost = languageData.getString("keyWordStatsGamesLost", "Games Lost");
		keyWordStatsDacsDone = languageData.getString("keyWordStatsDacsDone", "DaCs Achieved");
		keyWordStatsTimePlayed = languageData.getString("keyWordStatsTimePlayed", "Time Played");
		keyWordStatsMoneyGot = languageData.getString("keyWordStatsMoneyGot", "Money Got");
		keyWordStatsProgression = languageData.getString("keyWordStatsProgression", "Progression");
		keyWordStatsCompleted = languageData.getString("keyWordStatsCompleted", "Completed");
		keyWordStatsNotCompleted = languageData.getString("keyWordStatsNotCompleted", "Not Completed");

		keyWordHelp = languageData.getString("keyWordHelp", "Help");
		keyWordHelpCategory = languageData.getString("keyWordHelpCategory", "Category");
		keyWordHelpPage = languageData.getString("keyWordHelpPage", "Page");
		keyWordHelpAdvanced = languageData.getString("keyWordHelpAdvanced", "Advanced Information");
		keyWordHelpInformation = languageData.getString("keyWordHelpInformation", "Information");
		keyWordHelpCurrent = languageData.getString("keyWordHelpCurrent", "Current");
		keyWordHelpAmountPlayer = languageData.getString("keyWordHelpAmountPlayer", "amount of players");
		keyWordHelpWorld = languageData.getString("keyWordHelpWorld", "World");
		keyWordHelpLobby = languageData.getString("keyWordHelpLobby", "Lobby");
		keyWordHelpPlateform = languageData.getString("keyWordHelpPlateform", "Plateform");
		keyWordHelpPool = languageData.getString("keyWordHelpPool", "Pool");

		keyWordScoreboardPlayers = languageData.getString("keyWordScoreboardPlayers", "&6Players");
		keyWordScoreboardPoints = languageData.getString("keyWordScoreboardPoints", "&6Points");
		keyWordScoreboardRound = languageData.getString("keyWordScoreboardRound", "Round");

		keyWordJumpFast = languageData.getString("keyWordJumpFast", "Jump!");

		keyWordGuiPreviousPage = languageData.getString("keyWordGuiPreviousPage", "&dPrevious Page");
		keyWordGuiNextPage = languageData.getString("keyWordGuiNextPage", "&dNext Page");
		keyWordGuiInstrictions = languageData.getString("keyWordGuiInstrictions", "&6Instructions");

	}

	public void sendMsg(User user, String msg) {
		sendMsg(user.getPlayer(), msg);
	}

	public void sendMsg(Player player, String msg) {
		if(config.introInFrontOfEveryMessage) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefixShort + msg.toString()));
		} else {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg.toString()));
		}
	}

	public void sendMsg(Player player, Component msg) {
		if(config.introInFrontOfEveryMessage) {
			player.sendMessage(Component.join(Component.empty(), prefixShortComponent, msg));
		} else {
			player.sendMessage(msg);
		}
	}

	public CaseInsensitiveMap getCommandsDescription() {
		return commandDescriptions;
	}
}
