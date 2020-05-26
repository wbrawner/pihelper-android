/**
 * Copyright © 2019, 2020 William Brawner.
 *
 * This file is part of PiHelper.
 *
 * PiHelper is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PiHelper is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PiHelper.  If not, see <https://www.gnu.org/licenses/>.
 */
#ifndef PIHELPER
#define PIHELPER

/**
 * Use this with pihelper_set_log_level() to disable logging
 */
#define PIHELPER_LOG_DISABLED -1

/**
 * Use this with pihelper_set_log_level() to only log error messages
 */
#define PIHELPER_LOG_ERROR     0

/**
 * Use this with pihelper_set_log_level() to log error and warning messages
 */
#define PIHELPER_LOG_WARN      1

/**
 * Use this with pihelper_set_log_level() to log error, warning, and info messages
 */

#define PIHELPER_LOG_INFO      2

/**
 * Use this with pihelper_set_log_level() to print all log messages
 */
#define PIHELPER_LOG_DEBUG     3

/**
 * The return code used when a Pi-hole operation fails
 */
#define PIHELPER_OPERATION_FAILED -1

/**
 * The return code used to denote that the Pi-hole is enabled
 */
#define PIHELPER_ENABLED 0

/**
 * The return code used to denote that the Pi-hole is disabled
 */
#define PIHELPER_DISABLED 1

typedef struct {
    char * host;
    char * api_key;
} pihole_config;

/**
 * Call this to change the logging level with one of the PIHELPER_LOG_* constants
 */
void pihelper_set_log_level(int level);

/**
 * Retrieve the status of the pi-hole. This method does not require an API key to succeed.
 * @return PIHELPER_OPERATION_FAILED on failure, or either PIHELPER_ENABLED or PIHELPER_DISABLED
 */
int pihelper_get_status(pihole_config * config);

/**
 * Enable the pi-hole. This method requires a valid API key to succeed.
 * @return PIHELPER_OPERATION_FAILED on failure, or either PIHELPER_ENABLED or PIHELPER_DISABLED
 */
int pihelper_enable_pihole(pihole_config * config);

/**
 * Disable the pi-hole. This method requires a valid API key to succeed.
 * @return PIHELPER_OPERATION_FAILED on failure, or either PIHELPER_ENABLED or PIHELPER_DISABLED
 */
int pihelper_disable_pihole(pihole_config * config, char * duration);

/**
 * Create a new pihole_config object. The pointer returned here should be passed to pihelper_free_config()
 * when it is no longer needed;
 * @return A pointer to a pihole_config object or NULL if the system doesn't have enough memory
 */
pihole_config * pihelper_new_config();

/**
 * Sets the host for a given config. The memory will be copied, so you can free your copy once it's been
 * passed to the config object.
 */
void pihelper_config_set_host(pihole_config * config, char * host);

/**
 * Sets the password for a given config. The password will be hashed and converted to an API key. Call this
 * if you don't already have the API key, otherwise call pihelper_config_set_api_key(). The memory will be
 * copied, so you can free your copy once it's been passed to the config object.
 */
void pihelper_config_set_password(pihole_config * config, char * password);

/**
 * Sets the API key for a given config. The API key is a 64-character double SHA256 hash of the password. If
 * you don't have this, call pihelper_config_set_password() instead. The memory will be copied, so you can
 * free your copy once it's been passed to the config object.
 */
void pihelper_config_set_api_key(pihole_config * config, char * api_key);

/**
 * Read the PiHelper config from a file at the given path.
 */
pihole_config * pihelper_read_config(char * config_path);

/**
 * Save the PiHelper config to a file at the given path.
 */
int pihelper_save_config(pihole_config * config, char * config_path);

/**
 * Clean up all memory associated with a pihole_config object.
 */
void pihelper_free_config(pihole_config * config);
#endif

