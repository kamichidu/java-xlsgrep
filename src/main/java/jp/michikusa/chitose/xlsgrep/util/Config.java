package jp.michikusa.chitose.xlsgrep.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import lombok.NonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

public class Config
{
    public static void load(@NonNull Path path)
        throws IOException
    {
        try(final InputStream in= Files.newInputStream(path, StandardOpenOption.READ))
        {
            load(in);
        }
    }

    public static void load(@NonNull InputStream in)
        throws IOException
    {
        final Map<String, Object> newData;
        synchronized(yaml)
        {
            @SuppressWarnings("unchecked")
            final Map<String, Object> m= yaml.loadAs(in, Map.class);
            newData= m;
        }

        synchronized(data)
        {
            data.clear();
            data.putAll(newData);
        }
    }

    public <T> Optional<T> getAs(@NonNull CharSequence key, @NonNull Class<T> type)
    {
        synchronized(data)
        {
            return this.getAs(data,  key.toString(), type);
        }
    }

    private <T> Optional<T> getAs(@NonNull Map<String, Object> data, @NonNull String key, @NonNull Class<T> type)
    {
        if(key.contains("."))
        {
            try
            {
                final String firstKey= key.substring(0, key.indexOf("."));
                final String secondKey= key.substring(key.indexOf(".") + 1);
                @SuppressWarnings("unchecked")
                final Map<String, Object> nestedData= (Map<String, Object>)data.get(firstKey);
                if(nestedData == null)
                {
                    return Optional.empty();
                }

                return this.getAs(nestedData, secondKey, type);
            }
            catch(ClassCastException e)
            {
                logger.warn("A value associated `{}' is must be a hash.", key);
                return Optional.empty();
            }
        }

        final Object val= data.get(key);
        try
        {
            return Optional.ofNullable(type.cast(val));
        }
        catch(ClassCastException e)
        {
            logger.warn("A value associated `{}' is seems to be a `{}'. It's not applicable for `{}'.", key, val.getClass(), type);
            return Optional.empty();
        }
    }

    private static final Logger logger= LoggerFactory.getLogger(Config.class);

    private static final Yaml yaml= new Yaml();

    private static final Map<String, Object> data= new HashMap<>();
}
