/**
 * Copyright 2016 Stealth2800 <http://stealthyone.com/>
 * Copyright 2016 Contributors <https://github.com/FlexSeries>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.st28.flexseries.flexlib.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CommandHandler {

    /**
     * @return The label(s) for the command. If multiple are specified, the first one is used as the primary label.
     *         If separated by spaces, indicate subcommand levels.
     */
    String[] value();

    /**
     * @return True if this handler is the default subcommand for the parent.
     */
    boolean defaultSubcommand() default false;

    /**
     * @return The description of the command.
     */
    String description() default "";

    /**
     * @return The permission required to use the command.
     */
    String permission() default "";

    /**
     * @return True to make the command accessible only for players.
     */
    boolean playerOnly() default false;

    /**
     * @return The argument(s) for the command, in FeCAL.
     */
    String[] args() default "";

    /**
     * @return The auto argument(s) for the command, in FeCAL.
     */
    String[] autoArgs() default "";

}
