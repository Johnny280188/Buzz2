import { BrowserModule } from '@angular/platform-browser';
import { ErrorHandler, NgModule } from '@angular/core';
import { IonicApp, IonicErrorHandler, IonicModule } from 'ionic-angular';
import { SplashScreen } from '@ionic-native/splash-screen';
import { StatusBar } from '@ionic-native/status-bar';
import { Http, HttpModule } from '@angular/http';
import { MyApp } from './app.component';
import { HomePage, SignUpModal } from '../pages/home/home';
import { QueriesPage, NewqueryModal } from '../pages/queries/queries';
import { NewqueryPage } from '../pages/newquery/newquery';
import { ChanelsPage } from '../pages/chanels/chanels';
@NgModule({
    declarations: [
        MyApp,
        HomePage,
        QueriesPage,
        NewqueryPage,
        NewqueryModal,
        ChanelsPage,
        SignUpModal
    ],
    imports: [
        BrowserModule,
        IonicModule.forRoot(MyApp),
        HttpModule
    ],
    bootstrap: [IonicApp],
    entryComponents: [
        MyApp,
        HomePage,
        QueriesPage,
        NewqueryPage,
        NewqueryModal,
        ChanelsPage,
        SignUpModal
    ],
    providers: [
        StatusBar,
        SplashScreen,
        { provide: ErrorHandler, useClass: IonicErrorHandler }
    ]
})
export class AppModule {}
