/*
 * The MIT License
 * 
 * Copyright (c) 2011, Jesse Farinacci
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package hudson.plugins.dumpinfo;

import hudson.Extension;
import hudson.Launcher;
import hudson.PluginWrapper;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Computer;
import hudson.model.Hudson;
import hudson.model.JDK;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * This plugin allows jobs to automatically dump some important Jenkins-specific
 * information into the job log.
 * 
 * @author <a href="mailto:jieryn@gmail.com">Jesse Farinacci</a>
 * @since 1.0
 */
public final class DumpInfoBuildWrapper extends BuildWrapper
{
  /**
   * Plugin marker for BuildWrapper.
   */
  @Extension
  public static class DescriptorImpl extends BuildWrapperDescriptor
  {
    @Override
    public String getDisplayName()
    {
      return Messages.DumpInfo_DisplayName();
    }

    @Override
    public boolean isApplicable(final AbstractProject<?, ?> item)
    {
      return true;
    }
  }

  /**
   * Whether or not to dump information about Jenkins slave computers.
   * 
   * @see hudson.model.Hudson#getComputers()
   */
  private final boolean dumpComputers;

  /**
   * Whether or not to dump information about Jenkins JDK tools.
   * 
   * @see hudson.model.Hudson#getJDKs()
   */
  private final boolean dumpJdks;

  /**
   * Whether or not to dump information about Jenkins plugins.
   * 
   * @see hudson.model.Hudson#getPluginManager()
   * @see hudson.PluginManager#getPlugins()
   */
  private final boolean dumpPlugins;

  /**
   * Whether or not to dump information about Jenkins system properties.
   * 
   * @see SystemUtils#getSystemProperties()
   */
  private final boolean dumpSystemProperties;

  /**
   * Whether or not to dump information about Jenkins environment variables.
   * 
   * @see SystemUtils#getEnvironmentVariables()
   */
  private final boolean dumpEnvironmentVariables;

  /**
   * Whether or not to dump information about Jenkins JNDI bindings.
   * 
   * @see JndiUtils#getJndiBindings()
   * @since 1.2
   */
  private final boolean dumpJndi;

  /**
   * Configuration of this plugin is per-job.
   * 
   * @param dumpComputers
   *          whether or not to dump information about Jenkins slave computers
   * @param dumpJdks
   *          whether or not to dump information about Jenkins JDK tools
   * @param dumpPlugins
   *          whether or not to dump information about Jenkins plugins
   * @param dumpSystemProperties
   *          whether or not to dump information about Jenkins system properties
   * @param dumpEnvironmentVariables
   *          whether or not to dump information about Jenkins environment
   *          variables
   * 
   * @deprecated
   */
  public DumpInfoBuildWrapper(final boolean dumpComputers,
      final boolean dumpJdks, final boolean dumpPlugins,
      final boolean dumpSystemProperties, final boolean dumpEnvironmentVariables)
  {
    this(dumpComputers, dumpJdks, dumpPlugins, dumpSystemProperties,
        dumpEnvironmentVariables, true);
  }

  /**
   * Configuration of this plugin is per-job.
   * 
   * @param dumpComputers
   *          whether or not to dump information about Jenkins slave computers
   * @param dumpJdks
   *          whether or not to dump information about Jenkins JDK tools
   * @param dumpPlugins
   *          whether or not to dump information about Jenkins plugins
   * @param dumpSystemProperties
   *          whether or not to dump information about Jenkins system properties
   * @param dumpEnvironmentVariables
   *          whether or not to dump information about Jenkins environment
   *          variables
   * @param dumpJndi
   *          whether or not to dump information about Jenkins JNDI bindings
   * 
   * @since 1.2
   */
  @DataBoundConstructor
  public DumpInfoBuildWrapper(final boolean dumpComputers,
      final boolean dumpJdks, final boolean dumpPlugins,
      final boolean dumpSystemProperties,
      final boolean dumpEnvironmentVariables, final boolean dumpJndi)
  {
    super();

    this.dumpComputers = dumpComputers;
    this.dumpJdks = dumpJdks;
    this.dumpPlugins = dumpPlugins;
    this.dumpSystemProperties = dumpSystemProperties;
    this.dumpEnvironmentVariables = dumpEnvironmentVariables;
    this.dumpJndi = dumpJndi;
  }

  /**
   * Get whether or not to dump information about Jenkins slave computers.
   * 
   * @return whether or not to dump information about Jenkins slave computers
   */
  public boolean isDumpComputers()
  {
    return dumpComputers;
  }

  /**
   * Get whether or not to dump information about Jenkins JDK tools.
   * 
   * @return whether or not to dump information about Jenkins JDK tools
   */
  public boolean isDumpJdks()
  {
    return dumpJdks;
  }

  /**
   * Get whether or not to dump information about Jenkins plugins.
   * 
   * @return whether or not to dump information about Jenkins plugins
   */
  public boolean isDumpPlugins()
  {
    return dumpPlugins;
  }

  /**
   * Get whether or not to dump information about Jenkins system properties.
   * 
   * @return whether or not to dump information about Jenkins system properties
   */
  public boolean isDumpSystemProperties()
  {
    return dumpSystemProperties;
  }

  /**
   * Get whether or not to dump information about Jenkins environment variables.
   * 
   * @return whether or not to dump information about Jenkins environment
   *         variables
   */
  public boolean isDumpEnvironmentVariables()
  {
    return dumpEnvironmentVariables;
  }

  /**
   * Get whether or not to dump information about Jenkins JNDI bindings.
   * 
   * @return whether or not to dump information about Jenkins JNDI bindings
   */
  public boolean isDumpJndi()
  {
    return dumpJndi;
  }

  @Override
  public BuildWrapper.Environment setUp(
      @SuppressWarnings("rawtypes") final AbstractBuild build,
      final Launcher launcher, final BuildListener listener)
      throws IOException, InterruptedException
  {
    final Hudson hudson = Hudson.getInstance();
    final PrintStream logger = listener.getLogger();

    // ---

    logger.println(MessagesUtils.format(Hudson.getInstance()));

    if (dumpComputers)
    {
      for (final Computer computer : hudson.getComputers())
      {
        logger.println(MessagesUtils.format(computer));
      }
    }

    if (dumpJdks)
    {
      for (final JDK jdk : hudson.getJDKs())
      {
        logger.println(MessagesUtils.format(jdk));
      }
    }

    if (dumpPlugins)
    {
      for (final PluginWrapper plugin : hudson.getPluginManager().getPlugins())
      {
        logger.println(MessagesUtils.format(plugin));
      }
    }

    if (dumpSystemProperties)
    {
      for (final Map.Entry<String, String> entry : SystemUtils
          .getSystemProperties().entrySet())
      {
        logger.println(MessagesUtils.formatSystemProperty(entry.getKey(),
            entry.getValue()));
      }
    }

    if (dumpEnvironmentVariables)
    {
      for (final Map.Entry<String, String> entry : SystemUtils
          .getEnvironmentVariables().entrySet())
      {
        logger.println(MessagesUtils.formatEnvironmentVariable(entry.getKey(),
            entry.getValue()));
      }
    }

    if (dumpJndi)
    {
      for (final Map.Entry<String, String> entry : JndiUtils.getJndiBindings()
          .entrySet())
      {
        logger.println(MessagesUtils.formatJndiBinding(entry.getKey(),
            entry.getValue()));
      }
    }

    // ---

    return new Environment()
    {
      /* empty implementation */
    };
  }
}
