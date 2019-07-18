package cc.funkemunky.anticheat.impl.commands.kauri.arguments;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.utils.Message;
import cc.funkemunky.anticheat.api.utils.Messages;
import cc.funkemunky.api.commands.FunkeArgument;
import cc.funkemunky.api.commands.FunkeCommand;
import cc.funkemunky.api.utils.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class ReloadArgument extends FunkeArgument {
    public ReloadArgument(FunkeCommand parent, String name, String display, String description, String... permission) {
        super(parent, name, display, description, permission);

        addTabComplete(2, "full");
    }

    @Message(name = "command.reload.started")
    private String started = "&7Reloading Kauri...";

    @Message(name = "command.reload.completed")
    private String completed = "&aCompleted!";

    @Override
    public void onArgument(CommandSender commandSender, Command command, String[] args) {
        commandSender.sendMessage(Color.translate(started));
        if(args.length > 1) {
            if(args[1].equalsIgnoreCase("full")) {
                Kauri.getInstance().reloadKauri(true);
            } else commandSender.sendMessage(Color.translate(Messages.invalidArguments));
        } else {
            Kauri.getInstance().reloadKauri(false);
        }
        commandSender.sendMessage(Color.translate(completed));
    }
}
