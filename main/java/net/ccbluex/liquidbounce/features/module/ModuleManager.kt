/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.KeyEvent
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.features.module.modules.`fun`.ArmorFun
import net.ccbluex.liquidbounce.features.module.modules.`fun`.Derp
import net.ccbluex.liquidbounce.features.module.modules.`fun`.SkinDerp
import net.ccbluex.liquidbounce.features.module.modules.`fun`.TestModule
import net.ccbluex.liquidbounce.features.module.modules.combat.*
import net.ccbluex.liquidbounce.features.module.modules.exploit.*
import net.ccbluex.liquidbounce.features.module.modules.misc.*
import net.ccbluex.liquidbounce.features.module.modules.movement.*
import net.ccbluex.liquidbounce.features.module.modules.player.*
import net.ccbluex.liquidbounce.features.module.modules.render.*
import net.ccbluex.liquidbounce.features.module.modules.world.*
import net.ccbluex.liquidbounce.features.module.modules.world.Timer
import net.ccbluex.liquidbounce.utils.ClientUtils
import java.util.*


class ModuleManager : Listenable {

    val modules = TreeSet<Module> { module1, module2 -> module1.name.compareTo(module2.name) }
    private val moduleClassMap = hashMapOf<Class<*>, Module>()

    init {
        LiquidBounce.eventManager.registerListener(this)
    }

    val autoArmor = AutoArmor()
    val autoBow = AutoBow()
    val autoLeave = AutoLeave()
    val autoPot = AutoPot()
    val autoSoup = AutoSoup()
    val autoWeapon = AutoWeapon()
    val bowAimbot = BowAimbot()
    val criticals = Criticals()
    val killAura = KillAura()
    val trigger = Trigger()
    val velocity = Velocity()
    val fly = Fly()
    val clickGUI = ClickGUI()
    val highJump = HighJump()
    val inventoryMove = InventoryMove()
    val noSlow = NoSlow()
    val liquidWalk = LiquidWalk()
    val safeWalk = SafeWalk()
    val wallClimb = WallClimb()
    val strafe = Strafe()
    val sprint = Sprint()
    val teams = Teams()
    val noRotateSet = NoRotateSet()
    val antiBot = AntiBot()
    val chestStealer = ChestStealer()
    val scaffold = Scaffold()
    val civBreak = CivBreak()
    val tower = Tower()
    val fastBreak = FastBreak()
    val fastPlace = FastPlace()
    val esp = ESP()
    val speed = Speed()
    val tracers = Tracers()
    val nameTags = NameTags()
    val fastUse = FastUse()
    val teleport = Teleport()
    val fullbright = Fullbright()
    val itemESP = ItemESP()
    val storageESP = StorageESP()
    val projectiles = Projectiles()
    val noClip = NoClip()
    val nuker = Nuker()
    val pingSpoof = PingSpoof()
    val fastClimb = FastClimb()
    val step = Step()
    val autoRespawn = AutoRespawn()
    val autoTool = AutoTool()
    val noWeb = NoWeb()
    val spammer = Spammer()
    val iceSpeed = IceSpeed()
    val zoot = Zoot()
    val regen = Regen()
    val noFall = NoFall()
    val blink = Blink()
    val nameProtect = NameProtect()
    val noHurtCam = NoHurtCam()
    val ghost = Ghost()
    val midClick = MidClick()
    val xRay = XRay()
    val timer = Timer()
    val sneak = Sneak()
    val skinDerp = SkinDerp()
    val paralyze = Paralyze()
    val ghostHand = GhostHand()
    val autoWalk = AutoWalk()
    val autoBreak = AutoBreak()
    val freeCam = FreeCam()
    val aimbot = Aimbot()
    val eagle = Eagle()
    val hitBox = HitBox()
    val antiCactus = AntiCactus()
    val plugins = Plugins()
    val antiHunger = AntiHunger()
    val consoleSpammer = ConsoleSpammer()
    val longJump = LongJump()
    val parkour = Parkour()
    val ladderJump = LadderJump()
    val fastBow = FastBow()
    val multiActions = MultiActions()
    val airJump = AirJump()
    val autoClicker = AutoClicker()
    val noBob = NoBob()
    val blockOverlay = BlockOverlay()
    val noFriends = NoFriends()
    val blockESP = BlockESP()
    val chams = Chams()
    val clip = Clip()
    val phase = Phase()
    val serverCrasher = ServerCrasher()
    val noFOV = NoFOV()
    val fastStairs = FastStairs()
    val swingAnimation = SwingAnimation()
    val derp = Derp()
    val reverseStep = ReverseStep()
    val tntBlock = TNTBlock()
    val inventoryCleaner = InventoryCleaner()
    val trueSight = TrueSight()
    val liquidChat = LiquidChat()
    val antiBlind = AntiBlind()
    val noSwing = NoSwing()
    val bedGodMode = BedGodMode()
    val bugUp = BugUp()
    val breadcrumbs = Breadcrumbs()
    val abortBreaking = AbortBreaking()
    val potionSaver = PotionSaver()
    val cameraClip = CameraClip()
    val waterSpeed = WaterSpeed()
    val ignite = Ignite()
    val slimeJump = SlimeJump()
    val moreCarry = MoreCarry()
    val noPitchLimit = NoPitchLimit()
    val kick = Kick()
    val liquids = Liquids()
    val atAllProvider = AtAllProvider()
    val airLadder = AirLadder()
    val godMode = GodMode()
    val teleportHit = TeleportHit()
    val forceUnicodeChat = ForceUnicodeChat()
    val itemTeleport = ItemTeleport()
    val bufferSpeed = BufferSpeed()
    val superKnockback = SuperKnockback()
    val prophuntESP = ProphuntESP()
    val autoFish = AutoFish()
    val damage = Damage()
    val freeze = Freeze()
    val keepContainer = KeepContainer()
    val vehicleOneHit = VehicleOneHit()
    val reach = Reach()
    val rotations = Rotations()
    val noJumpDelay = NoJumpDelay()
    val blockWalk = BlockWalk()
    val antiAFK = AntiAFK()
    val perfectHorseJump = PerfectHorseJump()
    val hud = HUD()
    val tntesp = TNTESP()
    val componentOnHover = ComponentOnHover()
    val keepAlive = KeepAlive()
    val resourcePackSpoof = ResourcePackSpoof()
    val noSlowBreak = NoSlowBreak()
    val portalMenu = PortalMenu()
    val macroPacket = MacroPacket()
    val noScoreboard = NoScoreboard
    val fucker = Fucker
    val chestaura = ChestAura
    val auraTeams = AuraTeams()
    val boatTeleport = BoatTeleport()
    val infiniteAura = InfiniteAura()
    val perspectiveMod = PerspectiveMod()
    val legitSoup = LegitSoup()
    val godbridge = Godbridge()
    val armorFun = ArmorFun()
    val autoGapple = AutoGapple()
    val chatBypass = ChatBypass()
    val slientChestStealer = SlientChestStealer()
    val ambience = Ambience()
    val debugESP = DebugESP()
    val autoL = AutoL()
    val testModule = TestModule()

    /**
     * Register all modules
     */
    fun registerModules() {
        ClientUtils.getLogger().info("[ModuleManager] Loading modules...")

        registerModules(
            autoArmor,
            autoBow,
            autoLeave,
            autoPot,
            autoSoup,
            autoWeapon,
            bowAimbot,
            criticals,
            killAura,
            trigger,
            velocity,
            fly,
            clickGUI,
            highJump,
            inventoryMove,
            noSlow,
            liquidWalk,
            safeWalk,
            wallClimb,
            strafe,
            sprint,
            teams,
            noRotateSet,
            antiBot,
            chestStealer,
            scaffold,
            civBreak,
            tower,
            fastBreak,
            fastPlace,
            esp,
            speed,
            tracers,
            nameTags,
            fastUse,
            teleport,
            fullbright,
            itemESP,
            storageESP,
            projectiles,
            noClip,
            nuker,
            pingSpoof,
            fastClimb,
            step,
            autoRespawn,
            autoTool,
            noWeb,
            spammer,
            iceSpeed,
            zoot,
            regen,
            noFall,
            blink,
            nameProtect,
            noHurtCam,
            ghost,
            midClick,
            xRay,
            timer,
            sneak,
            skinDerp,
            paralyze,
            ghostHand,
            autoWalk,
            autoBreak,
            freeCam,
            aimbot,
            eagle,
            hitBox,
            antiCactus,
            plugins,
            antiHunger,
            consoleSpammer,
            longJump,
            parkour,
            ladderJump,
            fastBow,
            multiActions,
            airJump,
            autoClicker,
            noBob,
            blockOverlay,
            noFriends,
            blockESP,
            chams,
            clip,
            phase,
            serverCrasher,
            noFOV,
            fastStairs,
            swingAnimation,
            derp,
            reverseStep,
            tntBlock,
            inventoryCleaner,
            trueSight,
            liquidChat,
            antiBlind,
            noSwing,
            bedGodMode,
            bugUp,
            breadcrumbs,
            abortBreaking,
            potionSaver,
            cameraClip,
            waterSpeed,
            ignite,
            slimeJump,
            moreCarry,
            noPitchLimit,
            kick,
            liquids,
            atAllProvider,
            airLadder,
            godMode,
            teleportHit,
            forceUnicodeChat,
            itemTeleport,
            bufferSpeed,
            superKnockback,
            prophuntESP,
            autoFish,
            damage,
            freeze,
            keepContainer,
            vehicleOneHit,
            reach,
            rotations,
            noJumpDelay,
            blockWalk,
            antiAFK,
            perfectHorseJump,
            hud,
            tntesp,
            componentOnHover,
            keepAlive,
            resourcePackSpoof,
            noSlowBreak,
            portalMenu,
            macroPacket
        )

        registerModule(noScoreboard)
        registerModule(fucker)
        registerModule(chestaura)
        registerModule(auraTeams)
        registerModule(boatTeleport)
        registerModule(perspectiveMod)
        registerModule(legitSoup)
        registerModule(godbridge)
        registerModule(armorFun)
        registerModule(autoGapple)
        registerModule(chatBypass)
        registerModule(slientChestStealer)
        registerModule(ambience)
        registerModule(debugESP)
        registerModule(autoL)
        registerModule(testModule)

        ClientUtils.getLogger().info("[ModuleManager] Loaded ${modules.size} modules.")
    }

    /**
     * Register [module]
     */
    fun registerModule(module: Module) {
        modules += module
        moduleClassMap[module.javaClass] = module

        generateCommand(module)
        LiquidBounce.eventManager.registerListener(module)
    }

    /**
     * Register [moduleClass]
     */
    private fun registerModule(moduleClass: Class<out Module>) {
        try {
            registerModule(moduleClass)
        } catch (e: Throwable) {
            ClientUtils.getLogger()
                .error("Failed to load module: ${moduleClass.name} (${e.javaClass.name}: ${e.message})")
        }
    }

    /**
     * Register a list of modules
     */
    @SafeVarargs
    fun registerModules(vararg modules: Module) {
        modules.forEach(this::registerModule)
    }

    /**
     * Unregister module
     */
    fun unregisterModule(module: Module) {
        modules.remove(module)
        moduleClassMap.remove(module::class.java)
        LiquidBounce.eventManager.unregisterListener(module)
    }

    /**
     * Generate command for [module]
     */
    internal fun generateCommand(module: Module) {
        val values = module.values

        if (values.isEmpty())
            return

        LiquidBounce.commandManager.registerCommand(ModuleCommand(module, values))
    }

    /**
     * Legacy stuff
     *
     * TODO: Remove later when everything is translated to Kotlin
     */

    /**
     * Get module by [moduleClass]
     */
    fun getModule(moduleClass: Class<*>) = moduleClassMap[moduleClass]

    operator fun get(clazz: Class<*>) = getModule(clazz)

    /**
     * Get module by [moduleName]
     */
    fun getModule(moduleName: String?) = modules.find { it.name.equals(moduleName, ignoreCase = true) }

    /**
     * Module related events
     */

    /**
     * Handle incoming key presses
     */
    @EventTarget
    private fun onKey(event: KeyEvent) = modules.filter { it.keyBind == event.key }.forEach { it.toggle() }

    override fun handleEvents() = true
}
