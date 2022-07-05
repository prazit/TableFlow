package com.tflow.model.mapper;

import com.tflow.model.data.DatabaseData;
import com.tflow.model.data.LinePlugData;
import com.tflow.model.data.LocalData;
import com.tflow.model.data.SFTPData;
import com.tflow.model.editor.Line;
import com.tflow.model.editor.LinePlug;
import com.tflow.model.editor.datasource.DataSourceType;
import com.tflow.model.editor.datasource.Database;
import com.tflow.model.editor.datasource.Dbms;
import com.tflow.model.editor.datasource.Local;
import com.tflow.model.editor.datasource.SFTP;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2022-07-05T19:33:20+0700",
    comments = "version: 1.5.2.Final, compiler: javac, environment: Java 11.0.3 (JetBrains s.r.o)"
)
public class DataSourceMapperImpl implements DataSourceMapper {

    @Override
    public List<Integer> toLineList(List<Line> lineList) {
        if ( lineList == null ) {
            return null;
        }

        List<Integer> list = new ArrayList<Integer>( lineList.size() );
        for ( Line line : lineList ) {
            list.add( map( line ) );
        }

        return list;
    }

    @Override
    public List<Line> toIntegerList(List<Integer> lineList) {
        if ( lineList == null ) {
            return null;
        }

        List<Line> list = new ArrayList<Line>( lineList.size() );
        for ( Integer integer : lineList ) {
            list.add( map( integer ) );
        }

        return list;
    }

    @Override
    public Database map(DatabaseData databaseData) {
        if ( databaseData == null ) {
            return null;
        }

        Database database = new Database();

        database.setId( databaseData.getId() );
        if ( databaseData.getType() != null ) {
            database.setType( Enum.valueOf( DataSourceType.class, databaseData.getType() ) );
        }
        database.setName( databaseData.getName() );
        database.setImage( databaseData.getImage() );
        database.setPlug( linePlugDataToLinePlug( databaseData.getPlug() ) );
        if ( databaseData.getDbms() != null ) {
            database.setDbms( Enum.valueOf( Dbms.class, databaseData.getDbms() ) );
        }
        database.setUrl( databaseData.getUrl() );
        database.setDriver( databaseData.getDriver() );
        database.setUser( databaseData.getUser() );
        database.setUserEncrypted( databaseData.isUserEncrypted() );
        database.setPassword( databaseData.getPassword() );
        database.setPasswordEncrypted( databaseData.isPasswordEncrypted() );
        database.setRetry( databaseData.getRetry() );
        database.setQuotesForName( databaseData.getQuotesForName() );
        database.setQuotesForValue( databaseData.getQuotesForValue() );
        Map<String, String> map = databaseData.getPropList();
        if ( map != null ) {
            database.setPropList( new LinkedHashMap<String, String>( map ) );
        }

        return database;
    }

    @Override
    public DatabaseData map(Database database) {
        if ( database == null ) {
            return null;
        }

        DatabaseData databaseData = new DatabaseData();

        databaseData.setId( database.getId() );
        if ( database.getType() != null ) {
            databaseData.setType( database.getType().name() );
        }
        databaseData.setName( database.getName() );
        databaseData.setImage( database.getImage() );
        databaseData.setPlug( linePlugToLinePlugData( database.getPlug() ) );
        if ( database.getDbms() != null ) {
            databaseData.setDbms( database.getDbms().name() );
        }
        databaseData.setUrl( database.getUrl() );
        databaseData.setDriver( database.getDriver() );
        databaseData.setUser( database.getUser() );
        databaseData.setPassword( database.getPassword() );
        databaseData.setRetry( database.getRetry() );
        databaseData.setUserEncrypted( database.isUserEncrypted() );
        databaseData.setPasswordEncrypted( database.isPasswordEncrypted() );
        databaseData.setQuotesForName( database.getQuotesForName() );
        databaseData.setQuotesForValue( database.getQuotesForValue() );
        Map<String, String> map = database.getPropList();
        if ( map != null ) {
            databaseData.setPropList( new LinkedHashMap<String, String>( map ) );
        }

        return databaseData;
    }

    @Override
    public SFTP map(SFTPData sftpData) {
        if ( sftpData == null ) {
            return null;
        }

        SFTP sFTP = new SFTP();

        sFTP.setId( sftpData.getId() );
        if ( sftpData.getType() != null ) {
            sFTP.setType( Enum.valueOf( DataSourceType.class, sftpData.getType() ) );
        }
        sFTP.setName( sftpData.getName() );
        sFTP.setImage( sftpData.getImage() );
        sFTP.setPlug( linePlugDataToLinePlug( sftpData.getPlug() ) );
        List<String> list = sftpData.getPathHistory();
        if ( list != null ) {
            sFTP.setPathHistory( new ArrayList<String>( list ) );
        }
        sFTP.setRootPath( sftpData.getRootPath() );
        sFTP.setHost( sftpData.getHost() );
        sFTP.setPort( sftpData.getPort() );
        sFTP.setUser( sftpData.getUser() );
        sFTP.setPassword( sftpData.getPassword() );
        sFTP.setRetry( sftpData.getRetry() );
        sFTP.setTmp( sftpData.getTmp() );

        return sFTP;
    }

    @Override
    public SFTPData map(SFTP sftp) {
        if ( sftp == null ) {
            return null;
        }

        SFTPData sFTPData = new SFTPData();

        sFTPData.setId( sftp.getId() );
        if ( sftp.getType() != null ) {
            sFTPData.setType( sftp.getType().name() );
        }
        sFTPData.setName( sftp.getName() );
        sFTPData.setImage( sftp.getImage() );
        sFTPData.setPlug( linePlugToLinePlugData( sftp.getPlug() ) );
        List<String> list = sftp.getPathHistory();
        if ( list != null ) {
            sFTPData.setPathHistory( new ArrayList<String>( list ) );
        }
        sFTPData.setRootPath( sftp.getRootPath() );
        sFTPData.setHost( sftp.getHost() );
        sFTPData.setPort( sftp.getPort() );
        sFTPData.setUser( sftp.getUser() );
        sFTPData.setPassword( sftp.getPassword() );
        sFTPData.setRetry( sftp.getRetry() );
        sFTPData.setTmp( sftp.getTmp() );

        return sFTPData;
    }

    @Override
    public Local map(LocalData localData) {
        if ( localData == null ) {
            return null;
        }

        Local local = new Local();

        local.setId( localData.getId() );
        if ( localData.getType() != null ) {
            local.setType( Enum.valueOf( DataSourceType.class, localData.getType() ) );
        }
        local.setName( localData.getName() );
        local.setImage( localData.getImage() );
        local.setPlug( linePlugDataToLinePlug( localData.getPlug() ) );
        List<String> list = localData.getPathHistory();
        if ( list != null ) {
            local.setPathHistory( new ArrayList<String>( list ) );
        }
        local.setRootPath( localData.getRootPath() );

        return local;
    }

    @Override
    public LocalData map(Local local) {
        if ( local == null ) {
            return null;
        }

        LocalData localData = new LocalData();

        localData.setId( local.getId() );
        if ( local.getType() != null ) {
            localData.setType( local.getType().name() );
        }
        localData.setName( local.getName() );
        localData.setImage( local.getImage() );
        localData.setPlug( linePlugToLinePlugData( local.getPlug() ) );
        List<String> list = local.getPathHistory();
        if ( list != null ) {
            localData.setPathHistory( new ArrayList<String>( list ) );
        }
        localData.setRootPath( local.getRootPath() );

        return localData;
    }

    protected LinePlug linePlugDataToLinePlug(LinePlugData linePlugData) {
        if ( linePlugData == null ) {
            return null;
        }

        String plug = null;

        plug = linePlugData.getPlug();

        LinePlug linePlug = new LinePlug( plug );

        linePlug.setPlugged( linePlugData.isPlugged() );
        linePlug.setLineList( toIntegerList( linePlugData.getLineList() ) );
        linePlug.setRemoveButtonTip( linePlugData.getRemoveButtonTip() );
        linePlug.setRemoveButton( linePlugData.isRemoveButton() );
        linePlug.setExtractButton( linePlugData.isExtractButton() );
        linePlug.setTransferButton( linePlugData.isTransferButton() );
        linePlug.setLocked( linePlugData.isLocked() );
        linePlug.setStartPlug( linePlugData.isStartPlug() );

        return linePlug;
    }

    protected LinePlugData linePlugToLinePlugData(LinePlug linePlug) {
        if ( linePlug == null ) {
            return null;
        }

        LinePlugData linePlugData = new LinePlugData();

        linePlugData.setPlug( linePlug.getPlug() );
        linePlugData.setPlugged( linePlug.isPlugged() );
        linePlugData.setLineList( toLineList( linePlug.getLineList() ) );
        linePlugData.setRemoveButtonTip( linePlug.getRemoveButtonTip() );
        linePlugData.setRemoveButton( linePlug.isRemoveButton() );
        linePlugData.setExtractButton( linePlug.isExtractButton() );
        linePlugData.setTransferButton( linePlug.isTransferButton() );
        linePlugData.setLocked( linePlug.isLocked() );
        linePlugData.setStartPlug( linePlug.isStartPlug() );

        return linePlugData;
    }
}
