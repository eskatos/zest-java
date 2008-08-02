package org.qi4j.lib.struts2;

import static org.qi4j.lib.struts2.Constants.SERVLET_ATTRIBUTE;

import javax.servlet.FilterConfig;

import org.apache.struts2.config.BeanSelectionProvider;
import org.apache.struts2.dispatcher.Dispatcher;
import org.apache.struts2.dispatcher.FilterDispatcher;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.entity.UnitOfWorkFactory;
import org.qi4j.structure.Module;

import com.opensymphony.xwork2.config.ConfigurationException;
import com.opensymphony.xwork2.config.ConfigurationManager;
import com.opensymphony.xwork2.config.ContainerProvider;
import com.opensymphony.xwork2.inject.ContainerBuilder;
import com.opensymphony.xwork2.inject.Context;
import com.opensymphony.xwork2.inject.Factory;
import com.opensymphony.xwork2.inject.Scope;
import com.opensymphony.xwork2.util.location.LocatableProperties;

public class Qi4jFilterDispatcher extends FilterDispatcher
{   
    @Override
    protected Dispatcher createDispatcher( final FilterConfig filterConfig )
    {
        Dispatcher dispatcher = super.createDispatcher( filterConfig );
        ConfigurationManager configurationManager = new ConfigurationManager( BeanSelectionProvider.DEFAULT_BEAN_NAME );
        configurationManager.addContainerProvider(new ContainerProvider() {
            private boolean registered = false;
            
            public void register(ContainerBuilder builder, LocatableProperties props) throws ConfigurationException {
                builder.factory(UnitOfWorkFactory.class, new Factory<UnitOfWorkFactory>() {
                    public UnitOfWorkFactory create(Context context) throws Exception {
                        return getUnitOfWorkFactory(filterConfig);
                    }
                }, Scope.SINGLETON);
                builder.factory(CompositeBuilderFactory.class, new Factory<CompositeBuilderFactory>() {
                    public CompositeBuilderFactory create(Context context) throws Exception {
                        return getCompositeBuilderFactory(filterConfig);
                    }
                }, Scope.SINGLETON);
                builder.factory(ActionConfiguration.class, new Factory<ActionConfiguration>() {
                    public ActionConfiguration create(Context context) throws Exception {
                        return getActionConfiguration(filterConfig);
                    }
                }, Scope.SINGLETON);
                registered = true;
            }

            public boolean needsReload() {
                return !registered;
            }
            
            public void init(com.opensymphony.xwork2.config.Configuration configuration) throws ConfigurationException {}
            public void destroy() {}
        });
        dispatcher.setConfigurationManager(configurationManager);
        return dispatcher;
    }
        
    private UnitOfWorkFactory getUnitOfWorkFactory(FilterConfig filterConfig ) {
        return getModule(filterConfig).unitOfWorkFactory();
    }
    
    private CompositeBuilderFactory getCompositeBuilderFactory(FilterConfig filterConfig) {
        return getModule(filterConfig).compositeBuilderFactory();
    }
    
    private ActionConfiguration getActionConfiguration(FilterConfig filterConfig) {
        return getModule(filterConfig).serviceFinder().findService( ActionService.class ).metaInfo( ActionConfiguration.class );
    }

    private Module getModule(FilterConfig filterConfig) {
        return (Module) filterConfig.getServletContext().getAttribute( SERVLET_ATTRIBUTE );
    }
}
